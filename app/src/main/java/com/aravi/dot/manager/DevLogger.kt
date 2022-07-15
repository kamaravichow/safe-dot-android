package com.aravi.dot.manager

import com.aravi.dot.bean.DevLogs
import com.aravi.dot.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DevLogger(private val appDatabase: AppDatabase) {

    fun log(message: String) {
        CoroutineScope(Dispatchers.Default).launch {
            appDatabase.devLogsDao().newDevLog(DevLogs(System.currentTimeMillis(), message))
        }
    }

}
