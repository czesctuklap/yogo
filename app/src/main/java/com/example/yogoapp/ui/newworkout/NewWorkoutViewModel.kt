package com.example.yogoapp.ui.newworkout

import android.util.Log
import androidx.lifecycle.*

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

enum class Question { FITNESS, HEALTH, MAIN_GOAL, DURATION, ENERGY, PROPS, YOGA }

class NewWorkoutViewModel : ViewModel() {

    private val fitness   = MutableLiveData<String?>()
    private val health    = MutableLiveData<String?>()
    private val mainGoal  = MutableLiveData<String?>()
    private val duration  = MutableLiveData<String?>()
    private val energy    = MutableLiveData<String?>()
    private val props     = MutableLiveData<String?>()
    private val yoga      = MutableLiveData<String?>()

    private val _result = MutableLiveData<String>()
    val result: LiveData<String> = _result

    val isFormValid: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        fun recompute() {
            value = listOf(
                fitness.value, health.value, mainGoal.value,
                duration.value, energy.value, props.value, yoga.value
            ).all { !it.isNullOrBlank() }
        }
        listOf(fitness, health, mainGoal, duration, energy, props, yoga).forEach { src ->
            addSource(src) { recompute() }
        }
        value = false
    }

    fun onOptionSelected(question: Question, tagValue: String?) {
        when (question) {
            Question.FITNESS    -> fitness.value = tagValue
            Question.HEALTH     -> health.value = tagValue
            Question.MAIN_GOAL  -> mainGoal.value = tagValue
            Question.DURATION   -> duration.value = tagValue
            Question.ENERGY     -> energy.value = tagValue
            Question.PROPS      -> props.value = tagValue
            Question.YOGA       -> yoga.value = tagValue
        }
    }

    // 4:00–12:00 -> morning; 12:01–17:00 -> afternoon; 17:01–20:30 -> evening; 20:31–3:59 -> before_sleep
    private fun currentTimeBucket(): String {
        val cal = Calendar.getInstance()
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)

        fun afterOrEqual(hh: Int, mm: Int) = (h > hh) || (h == hh && m >= mm)
        fun beforeOrEqual(hh: Int, mm: Int) = (h < hh) || (h == hh && m <= mm)
        fun strictlyBetween(sh: Int, sm: Int, eh: Int, em: Int): Boolean {
            val afterStart = (h > sh) || (h == sh && m > sm)
            val beforeEnd  = (h < eh) || (h == eh && m <= em)
            return afterStart && beforeEnd
        }

        if (afterOrEqual(4, 0) && beforeOrEqual(12, 0)) return "morning"
        if (strictlyBetween(12, 0, 17, 0)) return "afternoon"
        if (strictlyBetween(17, 0, 20, 30)) return "evening"
        return "before_sleep"
    }

    fun submitIfValid(): String? {
        if (isFormValid.value != true) return null

        val timeOfDay = currentTimeBucket()

        val payload = listOf(
            energy.value.orEmpty(),
            props.value.orEmpty(),
            health.value.orEmpty(),
            mainGoal.value.orEmpty(),
            duration.value.orEmpty(),
            fitness.value.orEmpty(),
            yoga.value.orEmpty(),
            timeOfDay
        ).joinToString(";")

        Log.d("NewWorkoutResult", "Generated string: $payload")
        _result.value = payload
        return payload
    }
}
