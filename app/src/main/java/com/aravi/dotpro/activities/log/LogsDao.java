package com.aravi.dotpro.activities.log;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.aravi.dotpro.model.Logs;

import java.util.List;

@Dao
public interface LogsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Logs logs);

    @Query("DELETE FROM logs_database")
    void clearAllLogs();

    @Query("SELECT * FROM logs_database ORDER BY timestamp DESC")
    LiveData<List<Logs>> getOrderedLogs();
}
