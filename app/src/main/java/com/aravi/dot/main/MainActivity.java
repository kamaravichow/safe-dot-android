package com.aravi.dot.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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

import static com.aravi.dot.Constants.NATIVE_BANNER_PLACEMENT_ID;

public class MainActivity extends AppCompatActivity {

    private boolean TRIGGERED_START = false;
    private SwitchMaterial mainSwitch, vibrateSwitch, analyticsSwitch;
    private SharedPreferenceManager sharedPreferenceManager;
    private Intent serviceIntent;
    private NativeBannerAd mNativeBannerAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferenceManager = SharedPreferenceManager.getInstance(this);
        init();
        initAdvertisements();

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

    private void checkForAccessibilityAndStart() {
        if (!accessibilityPermission(getApplicationContext(), DotService.class)) {
            mainSwitch.setChecked(false);
            Utils.showPermissionsDialog(MainActivity.this);
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
        showSnack("Disable the accessibility permission to the app");
    }

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

    @Override
    protected void onResume() {
        super.onResume();
        Utils.dismissPermissionDialog();
        if (TRIGGERED_START) {
            TRIGGERED_START = false;
            checkForAccessibilityAndStart();
        }
        if (!sharedPreferenceManager.isServiceEnabled()) {
            mainSwitch.setChecked(checkAccessibility());
        }
    }

    private void sendFeedbackEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "kamaravichow@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for SafeDot");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Device Information : \n----- Don't clear these ----\n "
                + Build.DEVICE + " ,\n " + Build.BOARD + " ,\n " + Build.BRAND + " , " + Build.MANUFACTURER + " ,\n " + Build.MODEL + "\n ------ ");
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

    @Override
    protected void onDestroy() {
        if (mNativeBannerAd != null && mNativeBannerAd.isAdLoaded()) {
            mNativeBannerAd.destroy();
        }
        super.onDestroy();
    }
}