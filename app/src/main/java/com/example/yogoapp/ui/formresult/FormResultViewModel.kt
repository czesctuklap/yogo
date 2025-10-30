package com.example.yogoapp.ui.formresult

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.yogoapp.data.PrepopulatedDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.round

class FormResultViewModel(app: Application) : AndroidViewModel(app) {

    // Publiczny, aktualnie wybrany videoId do pokazania
    private val _videoId = MutableLiveData<String>()
    val videoId: LiveData<String> = _videoId

    // Cała lista dopasowanych url-i (ID YouTube) + indeks
    private val _urls = MutableLiveData<List<String>>(emptyList())
    private val _index = MutableLiveData(0)

    // Ustaw payload z ekranu formularza -> policz dopasowania i ustaw pierwszy wynik
    fun setUserInput(userInput: String) {
        viewModelScope.launch {
            val urls: List<String> = withContext(Dispatchers.IO) {
                val ctx = getApplication<Application>()
                PrepopulatedDb.ensureInstalled(ctx)
                val db = PrepopulatedDb.openReadOnly(ctx)

                try {
                    // ----- stałe (odpowiednik stałych w Pythonie) -----
                    val CATEGORY_WEIGHTS = mapOf(
                        "time_of_day" to 1.0,
                        "intensity"   to 2.5,
                        "level"       to 4.0,
                        "focus_area"  to 2.5,
                        "type"        to 2.0,
                        "goal"        to 1.75,
                        "props"       to 4.0
                    )

                    val LEVEL_WEIGHTED = mapOf(
                        "beginner"     to mapOf("beginner" to 1.0),
                        "intermediate" to mapOf("intermediate" to 1.0, "beginner" to 0.5),
                        "advanced"     to mapOf("advanced" to 1.0, "intermediate" to 0.7, "beginner" to 0.4)
                    )

                    val INTENSITY_HIERARCHY = mapOf(
                        "low"    to listOf("low"),
                        "medium" to listOf("medium", "low"),
                        "high"   to listOf("high", "medium")
                    )

                    fun getDurationRange(tags: Set<String>): IntRange {
                        return when {
                            "20minus" in tags -> 0..23
                            "2045"    in tags -> 20..45
                            "45plus"  in tags -> 45..999
                            else              -> 0..999
                        }
                    }

                    // ----- parse user input -----
                    val userTags = userInput.split(";")
                        .map { it.trim().lowercase() }
                        .filter { it.isNotEmpty() }
                        .toSet()

                    val userLevel     = LEVEL_WEIGHTED.keys.firstOrNull { it in userTags }
                    val userProps     = listOf("none", "blocks", "belt").firstOrNull { it in userTags }
                    val userIntensity = INTENSITY_HIERARCHY.keys.firstOrNull { it in userTags }
                    val durRange      = getDurationRange(userTags)

                    // ----- tag lookup z bazy: name -> category -----
                    val tagLookup = buildMap<String, String> {
                        db.rawQuery("SELECT name, category FROM tag", null).use { c ->
                            val nameIdx = c.getColumnIndexOrThrow("name")
                            val catIdx  = c.getColumnIndexOrThrow("category")
                            while (c.moveToNext()) {
                                put(c.getString(nameIdx).lowercase(), c.getString(catIdx))
                            }
                        }
                    }

                    // ----- zbuduj mapę practice -> dane + tagi -----
                    data class P(
                        val id: Long,
                        val title: String,
                        val url: String,
                        val duration: Int,
                        val tags: MutableList<Pair<String,String>> = mutableListOf() // (name, category)
                    )

                    val practices = LinkedHashMap<Long, P>()
                    db.rawQuery(
                        """
                        SELECT p.id, p.title, p.youtube_url, p.duration_minutes, t.name, t.category
                        FROM practice p
                        JOIN practice_tag pt ON p.id = pt.practice_id
                        JOIN tag t ON t.id = pt.tag_id
                        """.trimIndent(), null
                    ).use { c ->
                        val idIdx  = c.getColumnIndexOrThrow("id")
                        val tIdx   = c.getColumnIndexOrThrow("title")
                        val uIdx   = c.getColumnIndexOrThrow("youtube_url")
                        val dIdx   = c.getColumnIndexOrThrow("duration_minutes")
                        val nameIx = c.getColumnIndexOrThrow("name")
                        val catIx  = c.getColumnIndexOrThrow("category")
                        while (c.moveToNext()) {
                            val id  = c.getLong(idIdx)
                            val p   = practices.getOrPut(id) {
                                P(
                                    id = id,
                                    title = c.getString(tIdx),
                                    url = c.getString(uIdx),
                                    duration = c.getInt(dIdx)
                                )
                            }
                            p.tags += (c.getString(nameIx).lowercase() to c.getString(catIx))
                        }
                    }

                    // ----- scoring jak w Pythonie -----
                    val scored = mutableListOf<Pair<P, Double>>()

                    for (p in practices.values) {
                        // filtr długości
                        val dur = p.duration
                        if (dur !in durRange) continue

                        var valid = true
                        var score = 0.0

                        for ((tagName, category) in p.tags) {
                            when (category) {
                                "time_of_day" -> {
                                    // jeśli użytkownik nie podał time_of_day – nie penalizujemy ani nie nagradzamy
                                    if (userTags.contains(tagName)) {
                                        score += CATEGORY_WEIGHTS["time_of_day"] ?: 1.0
                                    }
                                }

                                "level" -> {
                                    if (userLevel != null) {
                                        val allowed = LEVEL_WEIGHTED[userLevel] ?: emptyMap()
                                        val w = allowed[tagName]
                                        if (w == null) {
                                            valid = false; break
                                        } else {
                                            score += (CATEGORY_WEIGHTS["level"] ?: 1.0) * w
                                        }
                                    }
                                }

                                "props" -> {
                                    if (userProps != null) {
                                        val ok = when (userProps) {
                                            "none"   -> tagName == "none"
                                            "blocks" -> tagName == "blocks" || tagName == "none"
                                            "belt"   -> tagName == "belt" || tagName == "none"
                                            else     -> true
                                        }
                                        if (!ok) { valid = false; break }
                                        if (tagName == userProps) score += CATEGORY_WEIGHTS["props"] ?: 1.0
                                    }
                                }

                                "intensity" -> {
                                    if (userIntensity != null) {
                                        val allowed = INTENSITY_HIERARCHY[userIntensity] ?: emptyList()
                                        if (tagName !in allowed) { valid = false; break }
                                        if (tagName == userIntensity) score += CATEGORY_WEIGHTS["intensity"] ?: 1.0
                                    }
                                }

                                else -> {
                                    // inne kategorie: jeśli tag z praktyki jest w zestawie usera – dodaj wagę kategorii (albo 1.0)
                                    if (tagName in userTags) {
                                        score += CATEGORY_WEIGHTS[category] ?: 1.0
                                    }
                                }
                            }
                        }

                        if (valid) scored += p to score
                    }

                    // sort malejąco po score i zwróć TYLKO url-e
                    scored.sortByDescending { it.second }
                    scored.take(50).map { it.first.url } // możesz ograniczyć np. do 50
                }

                catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                }

            } // withContext

            _urls.value = urls
            _index.value = 0
            _videoId.value = urls.firstOrNull().orEmpty()
        }
    }

    fun next() {
        val urls = _urls.value ?: return
        if (urls.isEmpty()) return
        val i = ((_index.value ?: 0) + 1) % urls.size
        _index.value = i
        _videoId.value = urls[i]
    }
}
