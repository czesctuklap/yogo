package com.example.yogoapp.ui.formresult

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FormResultViewModel : ViewModel() {

    private val _videoId = MutableLiveData<String>()
    val videoId: LiveData<String> = _videoId

    init {
        // Tu docelowo pobierzesz ID z bazy/repo (async).
        // Na razie na sztywno:
        _videoId.value = "IVoY5wOw5KU"
    }

    // Jeśli kiedyś będziesz zmieniać rekomendacje:
    fun setVideoId(id: String) {
        _videoId.value = id
    }
}