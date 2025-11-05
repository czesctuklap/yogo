package com.example.yogoapp.ui.foryou

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.yogoapp.data.PrepopulatedDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForYouViewModel(app: Application) : AndroidViewModel(app) {

    private val _videoId = MutableLiveData<String>()
    val videoId: LiveData<String> = _videoId

    init {
        loadRecommendation()
    }

    /**
     * Jeśli historia jest pusta -> wybierz losowy filmik z practice!!!!!!!!!!!!
     * Jeśli historia nie jest pusta -> jeszcze nic!!!!!!!!!
     */
    fun loadRecommendation() {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val picked: String? = withContext(Dispatchers.IO) {
                PrepopulatedDb.ensureInstalled(ctx)

                val db = PrepopulatedDb.openReadOnly(ctx)
                try {
                    val hasHistory = db.rawQuery(
                        "SELECT 1 FROM history LIMIT 1", null
                    ).use { c -> c.moveToFirst() }

                    if (!hasHistory) {
                        db.rawQuery(
                            "SELECT youtube_url FROM practice ORDER BY RANDOM() LIMIT 1",
                            null
                        ).use { c ->
                            if (c.moveToFirst()) c.getString(0) else null
                        }
                    } else {
                        null
                    }
                } finally {
                    db.close()
                }
            }

            if (!picked.isNullOrBlank()) {
                _videoId.value = picked
            }
        }
    }

    fun setVideoId(id: String) {
        _videoId.value = id
    }
}
