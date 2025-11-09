package com.example.yogoapp.ui.formresult

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.chaquo.python.Python
import com.chaquo.python.PyObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class FormResultViewModel(app: Application) : AndroidViewModel(app) {

    private val _videoId = MutableLiveData<String>()
    val videoId: LiveData<String> = _videoId

    private val _urls = MutableLiveData<List<String>>(emptyList())
    private val _index = MutableLiveData(0)

    fun setUserInput(userInput: String) {
        viewModelScope.launch {
            val urls: List<String> = withContext(Dispatchers.IO) {
                try {
                    val module = Python.getInstance().getModule("recommend_practice_based_on_answers")
                    val dbPath = getApplication<Application>().getDatabasePath("yogo_database_git").absolutePath
                    module.callAttr("set_db_path", dbPath)

                    val result: PyObject = module.callAttr("find_best_matches", userInput)
                    val json = JSONArray(result.toString())

                    val list = mutableListOf<String>()
                    for (i in 0 until json.length()) {
                        val url = json.getJSONObject(i).getString("youtube_url")
                        Log.d("RECO", "Recommend: $url")
                        list.add(url)
                    }

                    list
                } catch (e: Exception) {
                    Log.e("FormResultVM", "Python call failed", e)
                    emptyList()
                }
            }

            _urls.value = urls
            _index.value = 0
            _videoId.value = urls.firstOrNull().orEmpty()
        }
    }


    fun next() {
        val urls = _urls.value ?: return
        if (urls.isEmpty()) return
        val i = ((_index.value ?: 0) + 1) % urls.size
        _index.value = i
        _videoId.value = urls[i]
    }
}
