package com.aravi.dot

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.aravi.dot.manager.SharedPreferenceManager
import com.facebook.ads.AudienceNetworkAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AudienceNetworkAds.initialize(this)
        createNotificationChannels()
        // It's better to ask consent from the user but analytics collection is on by default.
        if (SharedPreferenceManager.getInstance(this)?.isAnalyticsEnabled == true) {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            if (FirebaseCrashlytics.getInstance().didCrashOnPreviousExecution()) {
                FirebaseCrashlytics.getInstance().sendUnsentReports()
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(Constants.DEFAULT_NOTIFICATION_CHANNEL, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }


}