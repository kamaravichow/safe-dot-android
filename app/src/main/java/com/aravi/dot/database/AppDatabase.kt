package com.aravi.dot.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aravi.dot.BuildConfig
import com.aravi.dot.bean.AppBean
import com.aravi.dot.bean.DevLogs
import com.aravi.dot.bean.Log
import com.aravi.dot.database.dao.DevLogsDao
import com.aravi.dot.database.dao.LogsDao
import org.koin.core.component.KoinComponent

@Database(entities = [Log::class, AppBean::class, DevLogs::class], version = 1, autoMigrations = [])
abstract class AppDatabase : RoomDatabase(), KoinComponent {

    abstract fun logsDao(): LogsDao
    abstract fun devLogsDao(): DevLogsDao

    companion object {
        fun database(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "${BuildConfig.APPLICATION_ID}.app.db"
            )
                .allowMainThreadQueries()
                .build()
        }
    }


}
