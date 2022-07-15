package com.aravi.dot.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.aravi.dot.bean.Log


@Dao
interface LogsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLog(log: Log)

    @Update
    suspend fun update(log: Log)

    @Delete
    suspend fun delete(log: Log)

    @Query("DELETE FROM access_logs")
    fun clearLogs()

    @Query("SELECT * FROM access_logs ORDER BY timestamp DESC")
    fun getAllLogs(): LiveData<List<Log>>

    @Query("SELECT * FROM access_logs WHERE permission=:permission ORDER BY timestamp DESC")
    fun getLogsForPermission(permission: String): LiveData<List<Log>>

    @Query("SELECT * FROM access_logs WHERE packageName=:packageName ORDER BY timestamp DESC")
    fun getLogsForPackage(packageName: String): LiveData<List<Log>>

    @Query("SELECT * FROM access_logs WHERE permission=:date ORDER BY timestamp DESC")
    fun getLogsForDate(date: String): LiveData<List<Log>>

    @Query("DELETE FROM access_logs WHERE permission=:permission")
    fun clearLogs(permission: String)

    @Query("DELETE FROM access_logs WHERE packageName=:packageName")
    fun clearAppLogs(packageName: String)

    @Query("SELECT COUNT(*) FROM access_logs WHERE permission=:permission")
    fun getLogsCount(permission: String): Int

    @Query("SELECT COUNT(*) FROM access_logs WHERE packageName=:packageName")
    fun getLogsCountForPackage(packageName: String): Int

    @Query("SELECT COUNT(*) FROM (SELECT DISTINCT packageName FROM access_logs WHERE permission=:permission and date=:date)")
    fun getLogsCount(permission: String, date: String): Int
}
