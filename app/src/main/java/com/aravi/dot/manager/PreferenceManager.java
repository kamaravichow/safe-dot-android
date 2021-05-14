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

package com.aravi.dot.manager;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.content.res.ResourcesCompat;

import com.aravi.dot.constant.Constants;
import com.aravi.dot.R;

public class PreferenceManager {
    private static PreferenceManager instance;
    private Application application;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor prefEditor;


    private class PREF_CONSTANTS {
        public static final String SERVICE_KEY = "me.aravi.dot.SERVICE";
        public static final String VIBRATION_KEY = "me.aravi.dot.CUSTOM.VIBRATION";
        public static final String PLACEMENT_KEY = "me.aravi.dot.ALIGNMENT";
        public static final String ICON_KEY = "me.aravi.dot.ICON";

        public static final String CAMERA_KEY = "me.aravi.dot.CAMERA";
        public static final String MIC_KEY = "me.aravi.dot.MICROPHONE";
        public static final String LOCATION_KEY = "me.aravi.dot.LOCATION";

        public static final String CAMERA_DOT_COLOR = "dot.camera.color";
        public static final String MIC_DOT_COLOR = "dot.mic.color";
        public static final String LOC_DOT_COLOR = "dot.loc.color";
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

    public void setServiceEnabled(boolean set) {
        prefEditor.putBoolean(PREF_CONSTANTS.SERVICE_KEY, set).apply();
    }


    public boolean isVibrationEnabled() {
        return sharedPreferences.getBoolean(PREF_CONSTANTS.VIBRATION_KEY, false);
    }

    public void setVibrationEnabled(boolean set) {
        prefEditor.putBoolean(PREF_CONSTANTS.VIBRATION_KEY, set).apply();
    }

    // --------- Mic customisations ------------

    public boolean isMicEnabled() {
        return sharedPreferences.getBoolean(PREF_CONSTANTS.MIC_KEY, true);
    }

    public void setMicEnabled(boolean set) {
        prefEditor.putBoolean(PREF_CONSTANTS.MIC_KEY, set).apply();
    }

    public int getMicDotColor() {
        return sharedPreferences.getInt(PREF_CONSTANTS.MIC_DOT_COLOR, ResourcesCompat.getColor(application.getResources(), R.color.orange_500, null));
    }

    public void setMicDotColor(int color) {
        prefEditor.putInt(PREF_CONSTANTS.MIC_DOT_COLOR, color).apply();
    }

    // --------- Camera customisations ------------

    public boolean isCameraEnabled() {
        return sharedPreferences.getBoolean(PREF_CONSTANTS.CAMERA_KEY, true);
    }

    public void setCameraEnabled(boolean set) {
        prefEditor.putBoolean(PREF_CONSTANTS.CAMERA_KEY, set).apply();
    }

    public int getCameraDotColor() {
        return sharedPreferences.getInt(PREF_CONSTANTS.CAMERA_DOT_COLOR, application.getColor(R.color.green_500));
    }

    public void setCameraDotColor(int color) {
        prefEditor.putInt(PREF_CONSTANTS.CAMERA_DOT_COLOR, color).apply();
    }


    // --------- Location customisations ------------
    public boolean isLocationEnabled() {
        return sharedPreferences.getBoolean(PREF_CONSTANTS.LOCATION_KEY, false);
    }

    public void setLocationEnabled(boolean set) {
        prefEditor.putBoolean(PREF_CONSTANTS.LOCATION_KEY, set).apply();
    }

    public int getLocationDotColor() {
        return sharedPreferences.getInt(PREF_CONSTANTS.LOC_DOT_COLOR, ResourcesCompat.getColor(application.getResources(), R.color.purple_500, null));
    }

    public void setLocationDotColor(int color) {
        prefEditor.putInt(PREF_CONSTANTS.LOC_DOT_COLOR, color).apply();
    }


    // --------- Dot customisations ------------
    public int getDotPosition() {
        return sharedPreferences.getInt(PREF_CONSTANTS.PLACEMENT_KEY, 1);
    }

    public void setDotPostion(int set) {
        prefEditor.putInt(PREF_CONSTANTS.PLACEMENT_KEY, set).apply();
    }

    public boolean isIconsEnabled() {
        return sharedPreferences.getBoolean(PREF_CONSTANTS.ICON_KEY, true);
    }

    public void setIconsEnabled(boolean set) {
        prefEditor.putBoolean(PREF_CONSTANTS.ICON_KEY, set).apply();
    }


    //--------- App -------

    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean("is_first", true);
    }

    public void setFirstLaunch() {
        prefEditor.putBoolean("is_first", false).apply();
    }

}
