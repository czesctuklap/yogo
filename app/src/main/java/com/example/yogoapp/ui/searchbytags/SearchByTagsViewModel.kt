package com.example.yogoapp.ui.searchbytags

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchByTagsViewModel : ViewModel() {

    private val _selectedTag = MutableLiveData<String>()
    val selectedTag: LiveData<String> get() = _selectedTag

    fun onTagClicked(rawLabel: String) {
        val normalized = rawLabel.removePrefix("#")   // "#full_body" -> "full_body"
        _selectedTag.value = normalized
        Log.d("SearchByTags", normalized)             // log do konsoli
    } // w tym miejscu sie bedzie dzialo na bazie shenenigans

    val videoIds = listOf(
        "AWM5ZNdWlqw",
        "AWM5ZNdWlqw",
        "AWM5ZNdWlqw",
        "AWM5ZNdWlqw"
    )

}
