package com.aravi.dot.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.AudioRecordingConfiguration;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.aravi.dot.R;
import com.aravi.dot.manager.SharedPreferenceManager;

import java.util.List;

public class DotService extends AccessibilityService {

    private FrameLayout hoverLayout;
    private ImageView dot_camera, dot_mic;

    private CameraManager cameraManager;
    private CameraManager.AvailabilityCallback cameraCallback;
    private AudioManager audioManager;
    private AudioManager.AudioRecordingCallback micCallback;
    private SharedPreferenceManager sharedPreferenceManager;

    private WindowManager.LayoutParams layoutParams;
    private WindowManager windowManager;

    @Override
    protected void onServiceConnected() {
        getDefaults();
        createHoverOverlay();
        initDotViews();
        initHardwareCallbacks();
    }

    private void getDefaults() {
        sharedPreferenceManager = SharedPreferenceManager.getInstance(getApplicationContext());
    }

    private void initHardwareCallbacks() {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraManager.registerAvailabilityCallback(getCameraCallback(), null);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.registerAudioRecordingCallback(getMicCallback(), null);
    }

    private CameraManager.AvailabilityCallback getCameraCallback() {
        cameraCallback = new CameraManager.AvailabilityCallback() {
            @Override
            public void onCameraAvailable(String cameraId) {
                super.onCameraAvailable(cameraId);
                hideCamDot();
            }

            @Override
            public void onCameraUnavailable(String cameraId) {
                super.onCameraUnavailable(cameraId);
                showCamDot();
                triggerVibration();
            }
        };
        return cameraCallback;
    }

    private AudioManager.AudioRecordingCallback getMicCallback() {
        micCallback = new AudioManager.AudioRecordingCallback() {
            @Override
            public void onRecordingConfigChanged(List<AudioRecordingConfiguration> configs) {
                if (configs.size() > 0) {
                    showMicDot();
                    triggerVibration();
                } else {
                    hideMicDot();
                }
            }
        };
        return micCallback;
    }

    private void setDotCustomColors() {
        setViewTint(dot_camera, "#4CAF50");
        setViewTint(dot_mic, "#FF9800");
    }

    private void setViewTint(ImageView imageView, String hex) {
        imageView.setColorFilter(Color.parseColor(hex), android.graphics.PorterDuff.Mode.SRC_IN);
    }

    private void triggerVibration() {
        if (sharedPreferenceManager.isVibrationEnabled()) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(500);
            }
        }
    }

    private void initDotViews() {
        dot_camera = hoverLayout.findViewById(R.id.dot_camera);
        dot_mic = hoverLayout.findViewById(R.id.dot_mic);
        setDotCustomColors();
        dot_camera.postDelayed(() -> {
            dot_camera.setVisibility(View.GONE);
            dot_mic.setVisibility(View.GONE);
        }, 500);

    }

    private void createHoverOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        hoverLayout = new FrameLayout(this);
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = getLayoutGravity();
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.dot_layout, hoverLayout);
        windowManager.addView(hoverLayout, layoutParams);
    }

    private void updateLayoutGravity() {
        layoutParams.gravity = getLayoutGravity();
        windowManager.updateViewLayout(hoverLayout, layoutParams);
    }

    private int getLayoutGravity() {
        int position = sharedPreferenceManager.getPosition();
        switch (position) {
            case 0:
                return Gravity.TOP | Gravity.START;
            case 1:
                return Gravity.TOP | Gravity.END;
            default:
                return Gravity.TOP | Gravity.END;
        }
    }

    private void showMicDot() {
        if (sharedPreferenceManager.isMicIndicatorEnabled()) {
            updateLayoutGravity();
            setDotCustomColors();
            dot_mic.setVisibility(View.VISIBLE);
        }
    }

    private void hideMicDot() {
        dot_mic.setVisibility(View.GONE);
    }

    private void showCamDot() {
        if (sharedPreferenceManager.isCameraIndicatorEnabled()) {
            updateLayoutGravity();
            setDotCustomColors();
            dot_camera.setVisibility(View.VISIBLE);
        }
    }

    private void hideCamDot() {
        dot_camera.setVisibility(View.GONE);
    }

    public void upScaleView(View view) {
        view.animate().scaleX(1f).scaleY(1f).setDuration(500);
    }

    public void downScaleView(View view) {
        view.animate().scaleX(0f).scaleY(0f).setDuration(500);
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    private void unRegisterCameraCallBack() {
        if (cameraManager != null && cameraCallback != null) {
            cameraManager.unregisterAvailabilityCallback(cameraCallback);
        }
    }

    private void unRegisterMicCallback() {
        if (audioManager != null && micCallback != null) {
            audioManager.unregisterAudioRecordingCallback(micCallback);
        }
    }

    @Override
    public void onDestroy() {
        unRegisterCameraCallBack();
        unRegisterMicCallback();
        super.onDestroy();

//        Removed because it made situation even worse
//        if (sharedPreferenceManager.isServiceEnabled()){
//            Intent broadcastIntent = new Intent();
//            broadcastIntent.setAction("restartservice");
//            broadcastIntent.setClass(this, Restarter.class);
//            this.sendBroadcast(broadcastIntent);
//            super.onDestroy();
//        }
//        else {
//
//        }

    }
}