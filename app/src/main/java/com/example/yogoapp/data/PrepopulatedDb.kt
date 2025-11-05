package com.example.yogoapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

object PrepopulatedDb {
    private const val DB_NAME = "yogo_database_git"

    fun ensureInstalled(context: Context) {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (dbFile.exists()) return
        dbFile.parentFile?.mkdirs()
        context.assets.open(DB_NAME).use { input ->
            dbFile.outputStream().use { output -> input.copyTo(output) }
        }
    }

    fun openReadOnly(context: Context): SQLiteDatabase {
        val path = context.getDatabasePath(DB_NAME).path
        return SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)
    }

    fun openWritable(context: Context): SQLiteDatabase {
        val path = context.getDatabasePath(DB_NAME).path
        return SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)
    }
}
