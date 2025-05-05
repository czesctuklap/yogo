package com.example.yogoapp.ui.myhistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyHistoryViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is my history Fragment"
    }
    val text: LiveData<String> = _text
}
