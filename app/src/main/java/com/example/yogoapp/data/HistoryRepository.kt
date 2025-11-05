package com.example.yogoapp.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object HistoryRepository {

    suspend fun logPlayback(context: Context, youtubeId: String) = withContext(Dispatchers.IO) {
        try {
            PrepopulatedDb.ensureInstalled(context)
            val db = PrepopulatedDb.openWritable(context)
            db.beginTransaction()
            try {
                val practiceId = db.rawQuery(

                    "SELECT id FROM practice WHERE youtube_url = ? LIMIT 1",
                    arrayOf(youtubeId)
                ).use { c -> if (c.moveToFirst()) c.getLong(0) else null }

                if (practiceId != null) {
                    db.execSQL("INSERT INTO history (practice) VALUES (?)", arrayOf(practiceId))
                    Log.d("HistoryRepository", "Inserted history for practice=$practiceId (video=$youtubeId)")
                } else {
                    Log.w("HistoryRepository", "No matching practice for video=$youtubeId")
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
                db.close()
            }
        } catch (t: Throwable) {
            Log.e("HistoryRepository", "logPlayback error", t)
        }
    }
}
