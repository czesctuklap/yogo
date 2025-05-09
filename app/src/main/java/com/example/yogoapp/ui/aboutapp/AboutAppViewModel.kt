package com.example.yogoapp.ui.aboutapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AboutAppViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "YogoApp is your personal yoga and wellness companion.\n\n" +
                "Discover guided workouts, track your progress, and find sessions tailored just for you — anytime, anywhere.\n\n" +
                "Breathe, move, and grow with YogoApp. "
    }
    val text: LiveData<String> = _text
}