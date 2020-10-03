package com.aravi.dot;

import android.content.Context;

public class Constants {
    public static final String SHARED_PREFERENCE_NAME = BuildConfig.APPLICATION_ID;
    public static final String TEMP_SHARED_PREFERENCE_NAME = BuildConfig.APPLICATION_ID + BuildConfig.VERSION_CODE;
    public static final int ACCESS_MODE = Context.MODE_PRIVATE;

    public static final String LOGS_PREFERENCE_NAME = "APP.USAGE.LOG";
    public static final String LOGS_PREFERENCE_TAG = "LOG.USAGE";

    public static String DEFAULT_NOTIFICATION_CHANNEL = "SAFE_DOT_NOTIFICATION";
    public static int NOTIFICATION_ID = 256;

    public static String STOP_SERVICE_ACTION = "SERVICE.FOREGROUND.STOP.NOW";




    public static final String INTERSTITIAL_PLACEMENT_ID = "244358406678589_319269205854175";
    public static final String NATIVE_BANNER_PLACEMENT_ID = "244358406678589_319269205854175";

}
