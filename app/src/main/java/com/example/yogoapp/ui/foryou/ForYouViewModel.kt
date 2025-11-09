package com.example.yogoapp.ui.foryou

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class ForYouViewModel(app: Application) : AndroidViewModel(app) {

    private val _videoId = MutableLiveData<String>()
    val videoId: LiveData<String> = _videoId

    private val videoList = mutableListOf<String>()
    private var currentIndex = 0

    init {
        loadRecommendation()
    }

    fun loadRecommendation() {
        viewModelScope.launch {
            val list: List<String>? = withContext(Dispatchers.IO) {
                try {
                    val module = Python.getInstance().getModule("fyp_algorithm")
                    val dbPath = getApplication<Application>().getDatabasePath("yogo_database_git").absolutePath
                    module.callAttr("set_db_path", dbPath)

                    val tod = currentTimeOfDay()
                    val result: PyObject = module.callAttr("recommend_for_you", tod, 10)
                    val rawList = result.asList().map { it.toString() }
                    Log.d("ForYouVM", "Full video list: $rawList")
                    rawList
                } catch (e: Exception) {
                    Log.e("ForYouVM", "Python call error", e)
                    null
                }
            }

            if (!list.isNullOrEmpty()) {
                videoList.clear()
                videoList.addAll(list)
                currentIndex = 0
                _videoId.value = videoList[0]
            }
        }
    }

    fun nextVideo() {
        if (videoList.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % videoList.size
            _videoId.value = videoList[currentIndex]
        } else {
            loadRecommendation()
        }
    }

    private fun currentTimeOfDay(): String {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        val totalMinutes = hour * 60 + minute

        val morningStart = 4 * 60
        val morningEnd = 12 * 60      // 12:00
        val afternoonEnd = 17 * 60    // 17:00
        val eveningEnd = 20 * 60 + 30 // 20:30

        return when {
            totalMinutes in morningStart..morningEnd -> "morning"
            totalMinutes in (morningEnd + 1)..afternoonEnd -> "afternoon"
            totalMinutes in (afternoonEnd + 1)..eveningEnd -> "evening"
            else -> "before_sleep"
        }
    }

    fun setVideoId(id: String) {
        _videoId.value = id
    }
}
