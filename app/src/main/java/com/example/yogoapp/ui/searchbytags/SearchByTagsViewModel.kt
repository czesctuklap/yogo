package com.example.yogoapp.ui.searchbytags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchByTagsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is search by tags Fragment"
    }
    val text: LiveData<String> = _text
}