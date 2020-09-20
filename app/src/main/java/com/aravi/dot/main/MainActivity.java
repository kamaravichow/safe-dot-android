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
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aravi.dot.BuildConfig;
import com.aravi.dot.R;
import com.aravi.dot.manager.SharedPreferenceManager;
import com.aravi.dot.service.DotService;
import com.facebook.ads.AbstractAdListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;
import com.facebook.ads.NativeBannerAdView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {

    public static final int ACCESSIBILITY_RESULT_CODE = 2002;
    private boolean TRIGGERED_START = false;
    private SwitchMaterial mainSwitch, vibrateSwitch;
    private SharedPreferenceManager sharedPreferenceManager;
    private MaterialButton submitFeedback;

    private InterstitialAd mInterstitalAd;
    private NativeBannerAd mNativeBannerAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferenceManager = SharedPreferenceManager.getInstance(this);

        mainSwitch = findViewById(R.id.mainSwitch);
        vibrateSwitch = findViewById(R.id.vibrationSwitch);
        submitFeedback = findViewById(R.id.submitFeedback);

        mainSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        vibrateSwitch.setOnCheckedChangeListener(onCheckedChangeListener);

        RadioGroup align = findViewById(R.id.align);
        align.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i) {
                case R.id.topLeft:
                    sharedPreferenceManager.setPosition(0);
                    break;
                case R.id.topRight:
                    sharedPreferenceManager.setPosition(1);
                    break;
                default:
                    break;
            }
        });

        submitFeedback.setOnClickListener(view -> sendFeedbackEmail());

        ((TextView) findViewById(R.id.versionText)).setText("VERSION - " + BuildConfig.VERSION_NAME);

        mInterstitalAd = new InterstitialAd(this, "244358406678589_319269205854175");
        mInterstitalAd.loadAd();
        mInterstitalAd.setAdListener(new AbstractAdListener() {
            @Override
            public void onInterstitialDismissed(Ad ad) {
                super.onInterstitialDismissed(ad);
                mInterstitalAd.loadAd();
            }
        });

        mNativeBannerAd = new NativeBannerAd(MainActivity.this, "244358406678589_244358583345238");
        mNativeBannerAd.setAdListener(listener);
        mNativeBannerAd.loadAd();


    }

    private SwitchMaterial.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            switch (compoundButton.getId()) {
                case R.id.mainSwitch:
                    if (b) {
                        checkForAccessibilityAndStart();
                        TRIGGERED_START = true;
                    } else {
                        stopService();
                    }
                    break;
                case R.id.vibrationSwitch:
                    sharedPreferenceManager.setVibrationEnabled(b);
                    break;
                default:
                    break;

            }
        }
    };

    private void checkForAccessibilityAndStart() {
        if (!accessibilityPermission(getApplicationContext(), DotService.class)) {
            mainSwitch.setChecked(false);
            startActivityForResult(new Intent("android.settings.ACCESSIBILITY_SETTINGS"), ACCESSIBILITY_RESULT_CODE);
        } else {
            if (mInterstitalAd.isAdLoaded()) {
                mInterstitalAd.show();
            }
            mainSwitch.setChecked(true);
            startService(new Intent(MainActivity.this, DotService.class));
        }
    }

    private void stopService() {
        stopService(new Intent(MainActivity.this, DotService.class));
        if (mInterstitalAd.isAdLoaded()) {
            mInterstitalAd.show();
        }
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


    private NativeAdListener listener = new NativeAdListener() {
        @Override
        public void onMediaDownloaded(Ad ad) {
        }

        @Override
        public void onError(Ad ad, AdError adError) {
        }

        @Override
        public void onAdLoaded(Ad ad) {
            View adView = NativeBannerAdView.render(MainActivity.this, mNativeBannerAd, NativeBannerAdView.Type.HEIGHT_100);
            LinearLayout nativeBannerAdContainer = (LinearLayout) findViewById(R.id.native_banner_ad_container);
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
        if (TRIGGERED_START) {
            checkForAccessibilityAndStart();
            TRIGGERED_START = false;
        }
    }

    private void sendFeedbackEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "kamaravichow@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for SafeDot");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Device Information ----- Don't clear these ---- "
                + Build.DEVICE + " , " + Build.BOARD + " , " + Build.BRAND + " , " + Build.MANUFACTURER + " , " + Build.MODEL + " ------ ");
        startActivity(Intent.createChooser(emailIntent, "Send feedback..."));
    }
}