package com.aravi.dot.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aravi.dot.bean.AppBean


@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addApp(app: AppBean)


    @Query("DELETE FROM app_analysis")
    fun clearAppList()

    @Query("SELECT * FROM app_analysis ORDER BY packageName ASC")
    fun allAppsList(): LiveData<List<AppBean>>


    @Query("SELECT COUNT(*) FROM app_analysis")
    fun appsCount(): Int

}
