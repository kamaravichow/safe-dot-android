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
}