package com.example.yogoapp.ui.privacypolicy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PrivacyPolicyViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "We collect only the data necessary to provide the best user experience. This includes your activity within the app. " +
                "We do not share or sell your data to third parties.\n\n" +
                "By using YoGo, you agree to the terms of this Privacy Policy.\n\n" +
                "We may update this document from time to time, and we encourage you to review it periodically."
    }
    val text: LiveData<String> = _text
}
