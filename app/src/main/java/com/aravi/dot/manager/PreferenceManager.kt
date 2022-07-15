/*
 * Copyright (C) 2021.  Aravind Chowdary (@kamaravichow)
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
import androidx.core.content.res.ResourcesCompat
import com.aravi.dot.BuildConfig
import com.aravi.dot.Constants
import com.aravi.dot.R

class PreferenceManager(val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    private val prefEditor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {

    }

    var isServiceEnabled: Boolean
        get() = sharedPreferences.getBoolean(PREF_CONSTANTS.SERVICE_KEY, false)
        set(set) {
            prefEditor.putBoolean(PREF_CONSTANTS.SERVICE_KEY, set).apply()
        }
    var isVibrationEnabled: Boolean
        get() = sharedPreferences.getBoolean(PREF_CONSTANTS.VIBRATION_KEY, false)
        set(set) {
            prefEditor.putBoolean(PREF_CONSTANTS.VIBRATION_KEY, set).apply()
        }

    // --------- Mic customisations ------------
    var isMicEnabled: Boolean
        get() = sharedPreferences.getBoolean(PREF_CONSTANTS.MIC_KEY, true)
        set(set) {
            prefEditor.putBoolean(PREF_CONSTANTS.MIC_KEY, set).apply()
        }
    var micDotColor: Int
        get() = sharedPreferences.getInt(
            PREF_CONSTANTS.MIC_DOT_COLOR, ResourcesCompat.getColor(
                context.resources, R.color.orange_500, null
            )
        )
        set(color) {
            prefEditor.putInt(PREF_CONSTANTS.MIC_DOT_COLOR, color).apply()
        }

    // --------- Camera customisations ------------
    var isCameraEnabled: Boolean
        get() = sharedPreferences.getBoolean(PREF_CONSTANTS.CAMERA_KEY, true)
        set(set) {
            prefEditor.putBoolean(PREF_CONSTANTS.CAMERA_KEY, set).apply()
        }
    var cameraDotColor: Int
        get() = sharedPreferences.getInt(
            PREF_CONSTANTS.CAMERA_DOT_COLOR,
            context.getColor(R.color.green_500)
        )
        set(color) {
            prefEditor.putInt(PREF_CONSTANTS.CAMERA_DOT_COLOR, color).apply()
        }

    // --------- Location customisations ------------
    var isLocationEnabled: Boolean
        get() = sharedPreferences.getBoolean(PREF_CONSTANTS.LOCATION_KEY, false)
        set(set) {
            prefEditor.putBoolean(PREF_CONSTANTS.LOCATION_KEY, set).apply()
        }
    var locationDotColor: Int
        get() = sharedPreferences.getInt(
            PREF_CONSTANTS.LOC_DOT_COLOR, ResourcesCompat.getColor(
                context.resources, R.color.purple_500, null
            )
        )
        set(color) {
            prefEditor.putInt(PREF_CONSTANTS.LOC_DOT_COLOR, color).apply()
        }

    // --------- Dot customisations ------------
    val dotPosition: Int
        get() = sharedPreferences.getInt(PREF_CONSTANTS.PLACEMENT_KEY, 1)

    fun setDotPostion(set: Int) {
        prefEditor.putInt(PREF_CONSTANTS.PLACEMENT_KEY, set).apply()
    }

    var isIconsEnabled: Boolean
        get() = sharedPreferences.getBoolean(PREF_CONSTANTS.ICON_KEY, true)
        set(set) {
            prefEditor.putBoolean(PREF_CONSTANTS.ICON_KEY, set).apply()
        }

    //--------- App -------
    val isFirstLaunch: Boolean
        get() = sharedPreferences.getBoolean("is_first", true)

    fun setFirstLaunch() {
        prefEditor.putBoolean("is_first", false).apply()
    }

    val dotIntegrity: Boolean
        get() = sharedPreferences.getBoolean(PREF_CONSTANTS.DOT_INTEGRITY, true)
    var integrity: Boolean
        get() = sharedPreferences.getBoolean(PREF_CONSTANTS.APP_INTEGRITY, true)
        set(i) {
            prefEditor.putBoolean(PREF_CONSTANTS.APP_INTEGRITY, i).apply()
            prefEditor.putBoolean(PREF_CONSTANTS.DOT_INTEGRITY, i).apply()
        }


    private object PREF_CONSTANTS {
        const val SERVICE_KEY = "me.aravi.dot.SERVICE"
        const val VIBRATION_KEY = "me.aravi.dot.CUSTOM.VIBRATION"
        const val PLACEMENT_KEY = "Xme.aravi.dot.ALIGNMENT"
        const val ICON_KEY = "me.aravi.dot.ICON"
        const val CAMERA_KEY = "me.aravi.dot.CAMERA"
        const val MIC_KEY = "me.aravi.dot.MICROPHONE"
        const val LOCATION_KEY = "me.aravi.dot.LOCATION"
        const val APP_INTEGRITY =
            "me.aravi.dot.int.e.g.rit.y" + BuildConfig.VERSION_NAME + "_a" + BuildConfig.VERSION_CODE
        const val CAMERA_DOT_COLOR = "dot.camera.color"
        const val MIC_DOT_COLOR = "dot.mic.color"
        const val LOC_DOT_COLOR = "dot.loc.color"
        const val DOT_INTEGRITY = "sacchai.nijayithi." + BuildConfig.VERSION_CODE
    }

}
