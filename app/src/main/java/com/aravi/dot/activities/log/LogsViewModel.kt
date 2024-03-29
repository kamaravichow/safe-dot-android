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
package com.aravi.dot.activities.log

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.aravi.dot.bean.Log
import com.aravi.dot.database.AppDatabase

/**
 * Created by Aravind Chowdary on
 */
class LogsViewModel(val database: AppDatabase) : ViewModel() {

    fun loadList(permission: String): LiveData<List<Log>> {
        return database.logsDao().getLogsForPermission(permission)
    }

}
