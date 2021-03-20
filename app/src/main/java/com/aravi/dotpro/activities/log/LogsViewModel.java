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

package com.aravi.dotpro.activities.log;

/**
 * Created by Aravind Chowdary on
 **/

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.aravi.dotpro.model.Logs;

import java.util.List;

public class LogsViewModel extends AndroidViewModel {
    private LogsRepository logsRepository;
    private final LiveData<List<Logs>> mLogsList;

    /**
     * @param application
     */
    public LogsViewModel(Application application) {
        super(application);
        logsRepository = new LogsRepository(application);
        mLogsList = logsRepository.getLogs();
    }

    public LiveData<List<Logs>> getmLogsList() {
        return mLogsList;
    }

    public void clearLogs() {
        logsRepository.clearLogs();
    }


}

