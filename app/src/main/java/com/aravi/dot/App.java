package com.aravi.dot;

import android.app.Application;

import com.facebook.ads.AudienceNetworkAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AudienceNetworkAds.initialize(this);

        if (FirebaseCrashlytics.getInstance().didCrashOnPreviousExecution()) {
            FirebaseCrashlytics.getInstance().sendUnsentReports();
        }
        FirebaseAnalytics.getInstance(this);

//        OneSignal.startInit(this)
//                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
//                .unsubscribeWhenNotificationsAreDisabled(false)
//                .init();
    }
}
