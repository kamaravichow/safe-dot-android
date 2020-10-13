package com.aravi.dot

import android.app.Application
import com.facebook.ads.AudienceNetworkAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AudienceNetworkAds.initialize(this)
        FirebaseAnalytics.getInstance(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        if (FirebaseCrashlytics.getInstance().didCrashOnPreviousExecution()) {
            FirebaseCrashlytics.getInstance().sendUnsentReports()
        }
    }
}