package com.example.yogoapp.ui.newworkout

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar

data class FormState(
    val fitness: String?,
    val health: String?,
    val mainGoal: String?,
    val duration: String?,
    val energy: String?,
    val props: String?,
    val yoga: String?
)

class NewWorkoutViewModel : ViewModel() {

    private val _result = MutableLiveData<String>()
    val result: LiveData<String> = _result

    // 4:00–12:00 -> morning
    // 12:01–17:00 -> afternoon
    // 17:01–20:30 -> evening
    // 20:31–3:59 -> before_sleep
    private fun currentTimeBucket(): String {
        val cal = Calendar.getInstance()
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)

        fun afterOrEqual(hh: Int, mm: Int) = (h > hh) || (h == hh && m >= mm)
        fun beforeOrEqual(hh: Int, mm: Int) = (h < hh) || (h == hh && m <= mm)
        fun strictlyBetween(startH: Int, startM: Int, endH: Int, endM: Int): Boolean {
            val afterStart = (h > startH) || (h == startH && m > startM)
            val beforeEnd  = (h < endH)   || (h == endH   && m <= endM)
            return afterStart && beforeEnd
        }

        if (afterOrEqual(4, 0) && beforeOrEqual(12, 0)) return "morning"
        if (strictlyBetween(12, 0, 17, 0)) return "afternoon"
        if (strictlyBetween(17, 0, 20, 30)) return "evening"
        return "before_sleep"
    }

    fun submit(state: FormState) {
        val fitness   = state.fitness   ?: "all"
        val health    = state.health    ?: "all"
        val mainGoal  = state.mainGoal  ?: "all"
        val duration  = state.duration  ?: "all"
        val energy    = state.energy    ?: "all"
        val props     = state.props     ?: "all"
        val yoga      = state.yoga      ?: "all"

        val timeOfDay = currentTimeBucket()

        val payload = listOf(
            energy, props, health, mainGoal, duration, fitness, yoga, timeOfDay
        ).joinToString(";")

        Log.d("NewWorkoutResult", "Generated string: $payload")
        _result.value = payload
    }
}
