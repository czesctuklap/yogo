package com.example.yogoapp.ui.myhistory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.yogoapp.data.PrepopulatedDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyHistoryViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        private const val PAGE_SIZE = 10
        private const val MAX_COUNT = 50
    }

    private val _videoIds = MutableLiveData<List<String>>()
    val videoIds: LiveData<List<String>> get() = _videoIds

    private val _hasMore = MutableLiveData(true)
    val hasMore: LiveData<Boolean> get() = _hasMore

    private var currentPage = 0

    fun loadInitial() {
        currentPage = 0
        loadPage(currentPage)
    }

    fun loadNext() {
        if (_hasMore.value != true) return
        currentPage += 1
        loadPage(currentPage)
    }

    private fun loadPage(page: Int) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val ids = withContext(Dispatchers.IO) {
                PrepopulatedDb.ensureInstalled(ctx)
                val db = PrepopulatedDb.openReadOnly(ctx)

                val offset = page * PAGE_SIZE
                val sql = """
                            SELECT sub.youtube_url
                            FROM (
                                SELECT 
                                    p.youtube_url AS youtube_url,
                                    h.practiced_at AS practiced_at,
                                    h.id AS h_id
                                FROM history h
                                JOIN practice p ON p.id = h.practice
                                ORDER BY h.practiced_at DESC, h.id DESC
                                LIMIT $MAX_COUNT
                            ) AS sub
                            ORDER BY sub.practiced_at DESC, sub.h_id DESC
                            LIMIT $PAGE_SIZE OFFSET $offset
                        """.trimIndent()


                val list = mutableListOf<String>()
                val cursor = db.rawQuery(sql, null)
                cursor.use { c ->
                    val col = c.getColumnIndexOrThrow("youtube_url")
                    while (c.moveToNext()) list += c.getString(col)
                }
                db.close()
                list
            }

            _videoIds.value = ids
            val offset = page * PAGE_SIZE
            _hasMore.value = ids.size == PAGE_SIZE && (offset + PAGE_SIZE) < MAX_COUNT
        }
    }
}
