/*
 * Copyright (c) 2021. Aravind Chowdary
 */

package com.aravi.dot.manager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Locale;

public class AnalyticsManager {
    private static AnalyticsManager instance;
    private FirebaseAnalytics analytics;
    private FirebaseCrashlytics crashlytics;

    /**
     * Analytics manager initialisation
     *
     * @param context
     * @return
     */
    public static AnalyticsManager getInstance(Context context) {
        if (instance == null) {
            instance = new AnalyticsManager(context);
        }
        return instance;
    }

    public AnalyticsManager(Context context) {
        this.analytics = FirebaseAnalytics.getInstance(context);
        this.crashlytics = FirebaseCrashlytics.getInstance();
        init();
    }

    /**
     * Initialisation method
     */
    private void init() {
        analytics.setAnalyticsCollectionEnabled(true);
        crashlytics.setCrashlyticsCollectionEnabled(true);
    }



    /**
     * Activity log method
     * this data is used to understand the behaviour of usage
     * helps understand what features users use the most
     */
    public void setActivity(Activity activity) {
        Bundle activityLog = new Bundle();
        activityLog.putString(FirebaseAnalytics.Param.SCREEN_NAME, activity.getTitle().toString());
        activityLog.putString(FirebaseAnalytics.Param.SCREEN_CLASS, activity.getLocalClassName());
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, activityLog);
    }



    /**
     * Gets the permission state
     *
     * @param permission
     * @param state
     */
    public void setPermissionStatus(String permission, boolean state) {
        Bundle bPermission = new Bundle();
        bPermission.putString("PERMISSION_NAME", permission);
        bPermission.putBoolean("PERMISSION_STATE", state);
        analytics.logEvent("PERMISSION_EVENT", bPermission);
    }

    /**
     * resets all the analytics data
     * use only in bad cases
     */
    public void resetAnalytics() {
        analytics.resetAnalyticsData();
    }


    /**
     * Record exeption
     * this method used to record exeptions or errors in the app at runtime
     */
    public void exception(Exception e) {
        crashlytics.recordException(e);
    }

    /**
     * this makes a log in crashylitics
     */
    public void makeCrashLog(String log) {
        crashlytics.log(log);
    }

    /**
     * Past check method
     * this checks if there are any unsent reports and sends them if exists
     * unsent can be caused by loosing internet etc,...
     */
    public void pastCheck() {
        crashlytics.checkForUnsentReports().addOnCompleteListener(task -> {
            if (task.getResult()) {
                crashlytics.sendUnsentReports();
            }
        });

    }


    public FirebaseAnalytics getAnalytics() {
        return analytics;
    }

    /**
     * checks if the app is crashed in last session
     */
    public boolean crashed() {
        return crashlytics.didCrashOnPreviousExecution();
    }

}
