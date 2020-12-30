/*
 * Copyright (C) 2020.  Aravind Chowdary (@kamaravichow)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aravi.dot.activities.log;

/**
 * Created by Aravind Chowdary on
 **/
import android.app.Application;

import androidx.lifecycle.LiveData;

import com.aravi.dotpro.model.Logs;

import java.util.List;

public class LogsRepository {

    private LogsDao logsDao;
    private LiveData<List<Logs>> logLiveData;

    public LogsRepository(Application application) {
        LogsRoomDatabase db = LogsRoomDatabase.getDatabase(application);
        logsDao = db.logsDao();
        logLiveData = logsDao.getOrderedLogs();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<Logs>> getLogs() {
        return logLiveData;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insertLog(Logs logs) {
        LogsRoomDatabase.databaseWriteExecutor.execute(() -> {
            logsDao.insert(logs);
        });
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void clearLogs() {
        LogsRoomDatabase.databaseWriteExecutor.execute(() -> {
            logsDao.clearAllLogs();
        });
    }
}

