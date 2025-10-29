package com.example.yogoapp.ui.searchbytags

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.yogoapp.data.PrepopulatedDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class SearchByTagsViewModel(app: Application) : AndroidViewModel(app) {
    private val _videoIds = MutableLiveData<List<String>>()
    val videoIds: LiveData<List<String>> get() = _videoIds

    fun onTagClicked(rawLabel: String) {
        val tag = rawLabel.removePrefix("#")
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val ids = withContext(Dispatchers.IO) {
                PrepopulatedDb.ensureInstalled(ctx)
                val db = PrepopulatedDb.openReadOnly(ctx)
                val list = mutableListOf<String>()
                val cursor = db.rawQuery(
                    """
                    SELECT p.youtube_url
                    FROM practice p
                    JOIN practice_tag pt ON pt.practice_id = p.id
                    JOIN tag t ON t.id = pt.tag_id
                    WHERE t.name = ?
                    """.trimIndent(),
                    arrayOf(tag)
                )
                cursor.use { c ->
                    val col = c.getColumnIndexOrThrow("youtube_url")
                    while (c.moveToNext()) list += c.getString(col)
                }
                db.close()
                if (list.size > 10) list.shuffled(Random(System.currentTimeMillis())).take(10)
                else list
            }
            _videoIds.value = ids
        }
    }
}
