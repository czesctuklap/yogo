package com.example.yogoapp.ui.foryou

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ForYouViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is for you Fragment"
    }
    val text: LiveData<String> = _text
}