package com.aravi.dot

import android.app.Application
import com.aravi.dot.manager.SharedPreferenceManager
import com.facebook.ads.AudienceNetworkAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AudienceNetworkAds.initialize(this)
        // It's better to ask consent from the user but analytics collection is on by default.
        if (SharedPreferenceManager.getInstance(this)?.isAnalyticsEnabled == true) {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            if (FirebaseCrashlytics.getInstance().didCrashOnPreviousExecution()) {
                FirebaseCrashlytics.getInstance().sendUnsentReports()
            }
        }
    }
}