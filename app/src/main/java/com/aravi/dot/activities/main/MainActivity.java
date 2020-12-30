/*
 * Copyright (C) 2020.  Aravind Chowdary (@kamaravichow)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aravi.dot.activities.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aravi.dot.BuildConfig;
import com.aravi.dot.R;
import com.aravi.dot.Utils;
import com.aravi.dot.activities.log.LogsActivity;
import com.aravi.dot.manager.SharedPreferenceManager;
import com.aravi.dot.service.DotService;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;
import com.facebook.ads.NativeBannerAdView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

import static com.aravi.dot.Constants.NATIVE_BANNER_PLACEMENT_ID;

public class MainActivity extends AppCompatActivity {

    private static final int MY_REQUEST_CODE = 1802;
    private boolean TRIGGERED_START = false;
    private SwitchMaterial mainSwitch, vibrateSwitch, analyticsSwitch;
    private SharedPreferenceManager sharedPreferenceManager;
    private Intent serviceIntent;
    private AppUpdateManager appUpdateManager;
    private final SwitchMaterial.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            int id = compoundButton.getId();
            if (id == R.id.mainSwitch) {
                if (b) {
                    checkForAccessibilityAndStart();
                    TRIGGERED_START = true;
                } else {
                    stopService();
                    TRIGGERED_START = false;
                }
            } else if (id == R.id.vibrationSwitch) {
                sharedPreferenceManager.setVibrationEnabled(b);

            } else if (id == R.id.analyticsSwitch) {
                sharedPreferenceManager.setAnalyticsEnabled(b);
                showSnack("Changes will be applied on app restart");
                // fixed: Resource IDs will be non-final in Android Gradle Plugin version 5.0, avoid using them in switch case statements
                // fix source : http://tools.android.com/tips/non-constant-fields
            }

        }
    };
    private NativeBannerAd mNativeBannerAd;
    private final NativeAdListener listener = new NativeAdListener() {
        @Override
        public void onMediaDownloaded(Ad ad) {
        }

        @Override
        public void onError(Ad ad, AdError adError) {
        }

        @Override
        public void onAdLoaded(Ad ad) {
            View adView = NativeBannerAdView.render(MainActivity.this, mNativeBannerAd, NativeBannerAdView.Type.HEIGHT_100);
            LinearLayout nativeBannerAdContainer = findViewById(R.id.native_banner_ad_container);
            nativeBannerAdContainer.addView(adView);

        }

        @Override
        public void onAdClicked(Ad ad) {

        }

        @Override
        public void onLoggingImpression(Ad ad) {

        }
    };

    public static boolean accessibilityPermission(Context context, Class<?> cls) {
        ComponentName componentName = new ComponentName(context, cls);
        String string = Settings.Secure.getString(context.getContentResolver(), "enabled_accessibility_services");
        if (string == null) {
            return false;
        }
        TextUtils.SimpleStringSplitter simpleStringSplitter = new TextUtils.SimpleStringSplitter(':');
        simpleStringSplitter.setString(string);
        while (simpleStringSplitter.hasNext()) {
            ComponentName unflattenFromString = ComponentName.unflattenFromString(simpleStringSplitter.next());
            if (unflattenFromString != null && unflattenFromString.equals(componentName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferenceManager = SharedPreferenceManager.getInstance(this);
        init();
        checkForAppUpdates();
        initAdvertisements();
        checkAutoStartRequirement();
    }

    private void init() {
        mainSwitch = findViewById(R.id.mainSwitch);
        vibrateSwitch = findViewById(R.id.vibrationSwitch);
        analyticsSwitch = findViewById(R.id.analyticsSwitch);
        MaterialButton submitFeedback = findViewById(R.id.submitFeedback);
        MaterialButton rateApp = findViewById(R.id.rateApp);
        MaterialButton premiumApp = findViewById(R.id.premiumVersion);
        RadioGroup align = findViewById(R.id.align);
        ((TextView) findViewById(R.id.versionText)).setText("VERSION - " + BuildConfig.VERSION_NAME);

        mainSwitch.setChecked(sharedPreferenceManager.isServiceEnabled() && checkAccessibility());
        vibrateSwitch.setChecked(sharedPreferenceManager.isVibrationEnabled());
        analyticsSwitch.setChecked(sharedPreferenceManager.isAnalyticsEnabled());
        mainSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        vibrateSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        analyticsSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        submitFeedback.setOnClickListener(view -> sendFeedbackEmail());
        align.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.topLeft) {
                sharedPreferenceManager.setPosition(0);
            } else if (i == R.id.topRight) {
                sharedPreferenceManager.setPosition(1);
            }
            // fixed: Resource IDs will be non-final in Android Gradle Plugin version 5.0, avoid using them in switch case statements
            // fix source : http://tools.android.com/tips/non-constant-fields
        });

        findViewById(R.id.twitter_button).setOnClickListener(v -> {
            String url = "https://www.twitter.com/kamaravichow";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        findViewById(R.id.github_button).setOnClickListener(v -> {
            String url = "https://www.github.com/kamaravichow";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        rateApp.setOnClickListener(view -> {
            String url = "https://play.google.com/store/apps/details?id=com.aravi.dot";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        premiumApp.setOnClickListener(view -> {
            String url = "https://play.google.com/store/apps/details?id=com.aravi.dotpro";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        findViewById(R.id.logsClickable).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LogsActivity.class);
            startActivity(intent);
        });
    }

    private void checkForAccessibilityAndStart() {
        if (!accessibilityPermission(getApplicationContext(), DotService.class)) {
            mainSwitch.setChecked(false);
            startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS"));
        } else {
            mainSwitch.setChecked(true);
            sharedPreferenceManager.setServiceEnabled(true);
            serviceIntent = new Intent(MainActivity.this, DotService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private void stopService() {
        if (isAccessibilityServiceRunning()) {
            sharedPreferenceManager.setServiceEnabled(false);
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
        showSnack(getString(R.string.close_app_note));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TRIGGERED_START) {
            TRIGGERED_START = false;
            checkForAccessibilityAndStart();
        }
        if (!sharedPreferenceManager.isServiceEnabled()) {
            mainSwitch.setChecked(checkAccessibility());
        }
    }


    private void checkForAppUpdates() {
        appUpdateManager = AppUpdateManagerFactory.create(MainActivity.this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, MY_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                showSnack(getString(R.string.update_failed_note));
                checkForAppUpdates();
            }
        }
    }


    private void sendFeedbackEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.feedback_email_address), null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Device Information : \n----- Don't clear these ----\n " + Build.DEVICE + " ,\n " + Build.BOARD + " ,\n " + Build.BRAND + " , " + Build.MANUFACTURER + " ,\n " + Build.MODEL + "\n ------ ");
        startActivity(Intent.createChooser(emailIntent, "Send feedback..."));
    }

    private void initAdvertisements() {
        mNativeBannerAd = new NativeBannerAd(MainActivity.this, NATIVE_BANNER_PLACEMENT_ID);
        mNativeBannerAd.setAdListener(listener);
        mNativeBannerAd.loadAd();
    }

    private boolean checkAccessibility() {
        AccessibilityManager manager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        return manager.isEnabled();
    }

    private boolean isAccessibilityServiceRunning() {
        String prefString = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return prefString != null && prefString.contains(this.getPackageName() + "/" + DotService.class.getName());
    }


    private void showSnack(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void checkAutoStartRequirement() {
        String manufacturer = android.os.Build.MANUFACTURER;
        if (sharedPreferenceManager.isFirstLaunch()) {
            if ("xiaomi".equalsIgnoreCase(manufacturer)
                    || ("oppo".equalsIgnoreCase(manufacturer))
                    || ("vivo".equalsIgnoreCase(manufacturer))
                    || ("Honor".equalsIgnoreCase(manufacturer))) {
                Utils.showAutoStartDialog(MainActivity.this);
                sharedPreferenceManager.setFirstLaunch();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mNativeBannerAd != null && mNativeBannerAd.isAdLoaded()) {
            mNativeBannerAd.destroy();
        }
        super.onDestroy();
    }
}