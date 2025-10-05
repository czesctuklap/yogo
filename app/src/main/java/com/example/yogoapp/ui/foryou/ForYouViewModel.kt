package com.example.yogoapp.ui.foryou

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ForYouViewModel : ViewModel() {

    private val _videoId = MutableLiveData<String>()
    val videoId: LiveData<String> = _videoId

    init {
        // Tu docelowo pobierzesz ID z bazy/repo (async).
        // Na razie na sztywno:
        _videoId.value = "jMrzkn4vIAQ"
    }

    // Jeśli kiedyś będziesz zmieniać rekomendacje:
    fun setVideoId(id: String) {
        _videoId.value = id
    }
}
