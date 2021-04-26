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

package com.aravi.dotpro.manager;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.aravi.dotpro.Constants;

public class PreferenceManager {
    private static PreferenceManager instance;
    private Application application;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor prefEditor;


    private class PREF_CONSTANTS {
        public static final String SERVICE_KEY = "";
        public static final String VIBRATION_KEY = "";
        public static final String PLACEMENT_KEY = "";

        public static final String CAMERA_KEY = "";
        public static final String MIC_KEY = "";
        public static final String LOCATION_KEY = "";
    }


    public static PreferenceManager getInstance(Application application) {
        if (instance == null) {
            instance = new PreferenceManager(application);
        }
        return instance;
    }

    @SuppressLint("CommitPrefEdits")
    public PreferenceManager(Application application) {
        this.application = application;
        this.sharedPreferences = application.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        this.prefEditor = sharedPreferences.edit();
    }


    public boolean isServiceEnabled() {
        return sharedPreferences.getBoolean(PREF_CONSTANTS.SERVICE_KEY, false);
    }

    public boolean isVibrationEnabled() {
        return sharedPreferences.getBoolean(PREF_CONSTANTS.VIBRATION_KEY, false);
    }

    public boolean isMicEnabled() {
        return sharedPreferences.getBoolean(PREF_CONSTANTS.MIC_KEY, true);
    }

    public boolean isCameraEnabled() {
        return sharedPreferences.getBoolean(PREF_CONSTANTS.CAMERA_KEY, true);
    }

    public boolean isLocationEnabled() {
        return sharedPreferences.getBoolean(PREF_CONSTANTS.LOCATION_KEY, true);
    }

    public int getDotPosition() {
        return sharedPreferences.getInt(PREF_CONSTANTS.PLACEMENT_KEY, 0);
    }



}
