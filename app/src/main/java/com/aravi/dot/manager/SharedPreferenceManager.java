package com.aravi.dot.manager;

import android.content.Context;
import android.content.SharedPreferences;

import static com.aravi.dot.Constants.ACCESS_MODE;
import static com.aravi.dot.Constants.SHARED_PREFERENCE_NAME;

public class SharedPreferenceManager {

    private static SharedPreferenceManager sharedPreferenceManager;
    private SharedPreferences sharedPreferences;
    private Context context;

    public static SharedPreferenceManager getInstance(Context context) {
        if (null == sharedPreferenceManager) {
            sharedPreferenceManager = new SharedPreferenceManager(context.getApplicationContext()); // Memory leak fixed 
        }
        return sharedPreferenceManager;
    }

    public SharedPreferenceManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, ACCESS_MODE);
    }

    public void setFirstLaunch(){
        setBoolean(context, "APP.FIRST_LAUNCH", false);
    }

    public boolean isFirstLaunch(){
        return getBoolean(context, "APP.FIRST_LAUNCH", true);
    }


    public void setServiceEnabled(boolean value) {
        setBoolean(context, "SERVICE.STATE", value);
    }

    public boolean isServiceEnabled() {
        return getBoolean(context, "SERVICE.STATE", false);
    }

    public void setCameraIndicatorEnabled(boolean value) {
        setBoolean(context, "CAMERA_DOT.STATE", value);
    }

    public boolean isCameraIndicatorEnabled() {
        return getBoolean(context, "CAMERA_DOT.STATE", true);
    }

    public void setCameraIndicatorColor(String value) {
        setString(context, "CAMERA_DOT.COLOR", value);
    }

    public String getCameraIndicatorColor() {
        return getString(context, "CAMERA_DOT.COLOR", "#FC6042");
    }

    public void setMicIndicatorEnabled(boolean value) {
        setBoolean(context, "MICROPHONE_DOT.STATE", value);
    }

    public boolean isMicIndicatorEnabled() {
        return getBoolean(context, "MICROPHONE_DOT.STATE", true);
    }

    public void setMicIndicatorColor(String value) {
        setString(context, "MICROPHONE_DOT.COLOR", value);
    }

    public String getMicIndicatorColor() {
        return getString(context, "MICROPHONE_DOT.COLOR", "#FFA840");
    }

    public void setNotificationEnabled(boolean value) {
        setBoolean(context, "NOTIFICATIONS.STATE", value);
    }

    public boolean isNotificationEnabled() {
        return getBoolean(context, "NOTIFICATIONS.STATE", false);
    }

    public void setVibrationEnabled(boolean value) {
        setBoolean(context, "VIBRATION.STATE", value);
    }

    public boolean isVibrationEnabled() {
        return getBoolean(context, "VIBRATION.STATE", false);
    }

    public void setPosition(int value) {
        setInteger(context, "DOT.POSITION", value);
    }

    public int getPosition() {
        return getInteger(context, "DOT.POSITION", 0);
    }


    // -----------------------------------------------------------------------------

    public void setString(Context context, String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(Context context, String key, String def_value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, ACCESS_MODE);
        return sharedPreferences.getString(key, def_value);
    }

    public void setInteger(Context context, String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, ACCESS_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInteger(Context context, String key, int def_value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, ACCESS_MODE);
        return sharedPreferences.getInt(key, def_value);
    }

    public void setBoolean(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, ACCESS_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(Context context, String key, boolean def_value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, ACCESS_MODE);
        return sharedPreferences.getBoolean(key, def_value);
    }


}
