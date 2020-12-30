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

package com.aravi.dot.manager

import android.content.Context
import android.content.SharedPreferences
import com.aravi.dot.Constants
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class SharedPreferenceManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Constants.ACCESS_MODE)

    fun setFirstLaunch() {
        setBoolean(context, "APP.FIRST_LAUNCH", false)
    }

    val isFirstLaunch: Boolean
        get() = getBoolean(context, "APP.FIRST_LAUNCH", true)
    var isServiceEnabled: Boolean
        get() = getBoolean(context, "SERVICE.STATE", false)
        set(value) {
            setBoolean(context, "SERVICE.STATE", value)
        }
    var isCameraIndicatorEnabled: Boolean
        get() = getBoolean(context, "CAMERA_DOT.STATE", true)
        set(value) {
            setBoolean(context, "CAMERA_DOT.STATE", value)
        }
    var cameraIndicatorColor: String?
        get() = getString(context, "CAMERA_DOT.COLOR", "#FC6042")
        set(value) {
            setString(context, "CAMERA_DOT.COLOR", value)
        }
    var isMicIndicatorEnabled: Boolean
        get() = getBoolean(context, "MICROPHONE_DOT.STATE", true)
        set(value) {
            setBoolean(context, "MICROPHONE_DOT.STATE", value)
        }
    var micIndicatorColor: String?
        get() = getString(context, "MICROPHONE_DOT.COLOR", "#FFA840")
        set(value) {
            setString(context, "MICROPHONE_DOT.COLOR", value)
        }
    var isNotificationEnabled: Boolean
        get() = getBoolean(context, "NOTIFICATIONS.STATE", false)
        set(value) {
            setBoolean(context, "NOTIFICATIONS.STATE", value)
        }
    var isVibrationEnabled: Boolean
        get() = getBoolean(context, "VIBRATION.STATE", false)
        set(value) {
            setBoolean(context, "VIBRATION.STATE", value)
        }
    var position: Int
        get() = getInteger(context, "DOT.POSITION", 1)
        set(value) {
            setInteger(context, "DOT.POSITION", value)
        }
    var isAnalyticsEnabled: Boolean
        get() = getBoolean(context, "ANALYTICS.STATE", true)    // Collection is enabled by default
        set(value) {
            setBoolean(context, "ANALYTICS.STATE", value)
            FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(value)
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(value)
        }

    // -----------------------------------------------------------------------------
    fun setString(context: Context?, key: String?, value: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(context: Context, key: String?, def_value: String?): String? {
        val sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Constants.ACCESS_MODE)
        return sharedPreferences.getString(key, def_value)
    }

    fun setInteger(context: Context, key: String?, value: Int) {
        val sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Constants.ACCESS_MODE)
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInteger(context: Context, key: String?, def_value: Int): Int {
        val sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Constants.ACCESS_MODE)
        return sharedPreferences.getInt(key, def_value)
    }

    fun setBoolean(context: Context, key: String?, value: Boolean) {
        val sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Constants.ACCESS_MODE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(context: Context, key: String?, def_value: Boolean): Boolean {
        val sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Constants.ACCESS_MODE)
        return sharedPreferences.getBoolean(key, def_value)
    }

    companion object {
        private var sharedPreferenceManager: SharedPreferenceManager? = null

        @JvmStatic
        fun getInstance(context: Context): SharedPreferenceManager? {
            if (sharedPreferenceManager == null) {
                sharedPreferenceManager = SharedPreferenceManager(context.applicationContext)
            }
            return sharedPreferenceManager
        }
    }

}