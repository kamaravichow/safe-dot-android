package com.aravi.dot.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aravi.dot.bean.DevLogs


@Dao
interface DevLogsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun newDevLog(log: DevLogs)


    @Query("DELETE FROM dev_logs")
    fun clearDevLogs()

    @Query("SELECT * FROM dev_logs ORDER BY timestamp DESC")
    fun devLogsAll(): LiveData<List<DevLogs>>


    @Query("SELECT COUNT(*) FROM dev_logs")
    fun devLogCount(): Int


}
