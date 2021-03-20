package com.aravi.dotpro.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.AudioRecordingConfiguration;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.aravi.dotpro.BuildConfig;
import com.aravi.dotpro.Constants;
import com.aravi.dotpro.R;
import com.aravi.dotpro.Utils;
import com.aravi.dotpro.activities.log.LogsRepository;
import com.aravi.dotpro.activities.main.MainActivity;
import com.aravi.dotpro.manager.SharedPreferenceManager;
import com.aravi.dotpro.model.Logs;

import java.util.List;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.aravi.dotpro.Constants.NOTIFICATION_ID;


public class DotService extends AccessibilityService {
    private static final String TAG = "DotService";

    private LogsRepository mLogsRepository;
    private boolean isCameraUnavailable = false;
    private boolean isMicUnavailable = false;
    private final boolean isOnUseNotificationEnabled = true;

    private boolean didCameraUseStart = false;
    private boolean didMicUseStart = false;

    private FrameLayout hoverLayout;
    private ImageView dotCamera, dotMic;

    private CameraManager cameraManager;
    private CameraManager.AvailabilityCallback cameraCallback;
    private AudioManager audioManager;
    private AudioManager.AudioRecordingCallback micCallback;
    private SharedPreferenceManager sharedPreferenceManager;

    private WindowManager.LayoutParams layoutParams;
    private WindowManager windowManager;

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notificationCompatBuilder;
    private String currentRunningAppPackage = BuildConfig.APPLICATION_ID;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Constants.SERVICE_NOTIFICATION_CHANNEL);
            @SuppressLint("WrongConstant") final Notification n = builder.setLocalOnly(true)
                    .setVisibility(Notification.VISIBILITY_PRIVATE)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setSmallIcon(R.drawable.transparent)
                    .build();
            startForeground(3, n);
        }

        getDefaults();
    }

    @Override
    protected void onServiceConnected() {
        createHoverOverlay();
        initDotViews();
        initHardwareCallbacks();
    }

    private void getDefaults() {
        mLogsRepository = new LogsRepository(getApplication());
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
                if (sharedPreferenceManager.isCameraIndicatorEnabled()) {
                    isCameraUnavailable = false;
                    didCameraUseStart = true;
                    dismissOnUseNotification();
                    hideCamDot();
                    makeLog();
                }

            }

            @Override
            public void onCameraUnavailable(String cameraId) {
                super.onCameraUnavailable(cameraId);
                if (sharedPreferenceManager.isCameraIndicatorEnabled()) {
                    isCameraUnavailable = true;
                    didCameraUseStart = true;
                    showOnUseNotification();
                    showCamDot();
                    triggerVibration();
                    makeLog();
                }

            }
        };
        return cameraCallback;
    }

    /**
     * @return
     */
    private AudioManager.AudioRecordingCallback getMicCallback() {
        micCallback = new AudioManager.AudioRecordingCallback() {
            @Override
            public void onRecordingConfigChanged(List<AudioRecordingConfiguration> configs) {
                if (sharedPreferenceManager.isMicIndicatorEnabled()) {
                    if (configs.size() > 0) {
                        showMicDot();
                        triggerVibration();
                        isMicUnavailable = true;
                        showOnUseNotification();

                    } else {
                        hideMicDot();
                        isMicUnavailable = false;
                        dismissOnUseNotification();
                    }
                    didMicUseStart = true;
                    makeLog();
                }


            }
        };
        return micCallback;
    }

    private void initOnUseNotification(String appUsingComponent) {
        notificationCompatBuilder = new NotificationCompat.Builder(getApplicationContext(), Constants.DEFAULT_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.transparent)
                .setContentTitle(getNotificationTitle())
                .setContentText(getNotificationDescription(appUsingComponent))
                .setContentIntent(getPendingIntent())
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager = NotificationManagerCompat.from(getApplicationContext());
    }

    /**
     * @return
     */
    private String getNotificationTitle() {
        if (isCameraUnavailable && isMicUnavailable)
            return "Camera and Mic are being accessed";
        if (isCameraUnavailable && !isMicUnavailable)
            return "Camera is being accessed";
        if (!isCameraUnavailable && isMicUnavailable)
            return "Mic is being accessed";
        return "Your Camera or Mic is ON";
    }

    /**
     * @param appUsingComponent
     * @return
     */
    private String getNotificationDescription(String appUsingComponent) {
        if (appUsingComponent.isEmpty() || appUsingComponent.equals("(unknown)")) {
            appUsingComponent = "some app";
        }

        if (isCameraUnavailable && isMicUnavailable)
            return "Hey, " + appUsingComponent + " is watching and hearing you";
        if (isCameraUnavailable && !isMicUnavailable)
            return "You're being watched by " + appUsingComponent + "!";
        if (!isCameraUnavailable && isMicUnavailable)
            return Utils.capitalizeFirstLetterOfString(appUsingComponent) + " is hearing you !";
        return "Looks like " + appUsingComponent + " is using your camera and mic...";
    }

    private void showOnUseNotification() {
        if (isOnUseNotificationEnabled) {
            initOnUseNotification(Utils.getNameFromPackageName(this, currentRunningAppPackage));
            if (notificationManager != null)
                notificationManager.notify(NOTIFICATION_ID, notificationCompatBuilder.build());
        }
    }

    private void dismissOnUseNotification() {
        if (isCameraUnavailable || isMicUnavailable) {
            showOnUseNotification();
        } else {
            if (notificationManager != null) notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    /**
     * @return
     */
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        return PendingIntent.getActivity(getApplicationContext(), 1, intent, FLAG_UPDATE_CURRENT);
    }


    private void makeLog() {
        int cameraState = 0;
        int micState = 0;

        if (didCameraUseStart && isCameraUnavailable) {
            cameraState = 1;
        } else {
            if (didCameraUseStart && !isCameraUnavailable) {
                cameraState = 2;
                didCameraUseStart = false;
            }
        }

        if (didMicUseStart && isMicUnavailable) {
            micState = 1;
        } else {
            if (didMicUseStart && !isMicUnavailable) {
                micState = 2;
                didMicUseStart = false;
            }
        }
        if (!currentRunningAppPackage.equals("com.aravi.dotpro")) {
            Logs log = new Logs(System.currentTimeMillis(), currentRunningAppPackage, cameraState, micState);
            mLogsRepository.insertLog(log);
        }

    }

    private void setDotCustomColors() {
        setViewTint(dotCamera, "#4CAF50");
        setViewTint(dotMic, "#FF9800");
    }

    /**
     * @param imageView
     * @param hex
     */
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
        dotCamera = hoverLayout.findViewById(R.id.dot_camera);
        dotMic = hoverLayout.findViewById(R.id.dot_mic);
        setDotCustomColors();
        dotCamera.postDelayed(() -> {
            dotCamera.setVisibility(View.GONE);
            dotMic.setVisibility(View.GONE);
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

    /**
     * @return
     */
    private int getLayoutGravity() {
        int position = sharedPreferenceManager.getPosition();
        switch (position) {
            case 0:
                return Gravity.TOP | Gravity.START;
            case 1:
                return Gravity.TOP | Gravity.END;
            case 2:
                return Gravity.BOTTOM | Gravity.START;
            case 3:
                return Gravity.BOTTOM | Gravity.END;
            default:
                return Gravity.TOP | Gravity.END;
        }
    }

    private void showMicDot() {
        if (sharedPreferenceManager.isMicIndicatorEnabled()) {
            updateLayoutGravity();
            setDotCustomColors();
            dotMic.setVisibility(View.VISIBLE);
        }
    }

    private void hideMicDot() {
        dotMic.setVisibility(View.GONE);
    }

    private void showCamDot() {
        if (sharedPreferenceManager.isCameraIndicatorEnabled()) {
            updateLayoutGravity();
            setDotCustomColors();
            dotCamera.setVisibility(View.VISIBLE);
        }
    }

    private void hideCamDot() {
        dotCamera.setVisibility(View.GONE);
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
        try {
            if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && accessibilityEvent.getPackageName() != null) {
                ComponentName componentName = new ComponentName(accessibilityEvent.getPackageName().toString(), accessibilityEvent.getClassName().toString());
                currentRunningAppPackage = componentName.getPackageName();
            }
        } catch (Exception ignored) {
            Log.i(TAG, "onAccessibilityEvent:" + new Exception(ignored).getMessage());
        }

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
        if (notificationManager != null) {
            notificationManager.cancel(3);
        }
        stopForeground(true);
        super.onDestroy();

    }
}