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
        FirebaseAnalytics.getInstance(this);
        if (FirebaseCrashlytics.getInstance().didCrashOnPreviousExecution()) {
            FirebaseCrashlytics.getInstance().sendUnsentReports();
        }
    }

}
