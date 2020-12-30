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

package com.aravi.dot

import android.content.Context

object Constants {
    const val SHARED_PREFERENCE_NAME = BuildConfig.APPLICATION_ID
    const val TEMP_SHARED_PREFERENCE_NAME = BuildConfig.APPLICATION_ID + BuildConfig.VERSION_CODE
    const val ACCESS_MODE = Context.MODE_PRIVATE
    const val LOGS_PREFERENCE_NAME = "APP.USAGE.LOG"
    const val LOGS_PREFERENCE_TAG = "LOG.USAGE"

    @JvmField
    var DEFAULT_NOTIFICATION_CHANNEL = "SAFE_DOT_NOTIFICATION"

    @JvmField
    var NOTIFICATION_ID = 256
    const val NATIVE_BANNER_PLACEMENT_ID = "244358406678589_244358583345238"
}