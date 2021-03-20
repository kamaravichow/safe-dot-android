package com.aravi.dotpro

import android.content.Context

object Constants {
    const val SHARED_PREFERENCE_NAME = BuildConfig.APPLICATION_ID
    const val ACCESS_MODE = Context.MODE_PRIVATE

    @JvmField
    var DEFAULT_NOTIFICATION_CHANNEL = "SAFE_DOT_PRO_NOTIFICATION"

    @JvmField
    var SERVICE_NOTIFICATION_CHANNEL = "SAFE_DOT_PRO_NOTIFICATION"

    @JvmField
    var NOTIFICATION_ID = 256
}