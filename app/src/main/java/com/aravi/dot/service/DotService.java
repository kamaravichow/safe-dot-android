package com.aravi.dot.service;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.aravi.dot.constant.Constants.NOTIFICATION_ID;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraManager;
import android.location.GnssStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
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

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.aravi.dot.BuildConfig;
import com.aravi.dot.R;
import com.aravi.dot.activities.log.database.LogsRepository;
import com.aravi.dot.activities.main.MainActivity;
import com.aravi.dot.constant.Constants;
import com.aravi.dot.manager.PreferenceManager;
import com.aravi.dot.model.Logs;
import com.aravi.dot.util.Utils;

import java.util.List;


public class DotService extends AccessibilityService {
    private static final String TAG = "DotService";

    private LogsRepository mLogsRepository;
    private boolean isCameraUnavailable = false;
    private boolean isMicUnavailable = false;
    private boolean isLocUnavailable = false;
    private final boolean isOnUseNotificationEnabled = true;

    private boolean didCameraUseStart = false;
    private boolean didMicUseStart = false;
    private boolean didLocUseStart = false;

    private FrameLayout hoverLayout;
    private ImageView dotCamera, dotMic, dotLoc;

    private CameraManager cameraManager;
    private CameraManager.AvailabilityCallback cameraCallback;
    private AudioManager audioManager;
    private AudioManager.AudioRecordingCallback micCallback;
    private LocationManager locationManager;


    private PreferenceManager sharedPreferenceManager;

    private WindowManager.LayoutParams layoutParams;
    private WindowManager windowManager;

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notificationCompatBuilder;
    private String currentRunningAppPackage = BuildConfig.APPLICATION_ID;
    private MutableLiveData<String> currentLivePackage = new MutableLiveData<>();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Notification notification = new Notification.Builder(this, Constants.SERVICE_NOTIFICATION_CHANNEL)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.notification_ContentText))
                    .setSmallIcon(R.drawable.transparent)
                    .setContentIntent(pendingIntent)
                    .setTicker(getString(R.string.notification_ticker))
                    .build();

            currentLivePackage.setValue(BuildConfig.APPLICATION_ID);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                startForeground(3, notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                                | ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                                | ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(3, notification);
            }
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
        sharedPreferenceManager = PreferenceManager.getInstance(getApplication());
    }

    private void initHardwareCallbacks() {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraManager.registerAvailabilityCallback(getCameraCallback(), null);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            audioManager.registerAudioRecordingCallback(getMicCallback(), null);
        }


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locationManager.registerGnssStatusCallback(locationCallback);
            }
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 8.4f, locationListener);
//            locationManager.removeUpdates(locationListener);

        } else {
            sharedPreferenceManager.setLocationEnabled(false);
        }

    }


    // Camera service callbacks
    private CameraManager.AvailabilityCallback getCameraCallback() {
        cameraCallback = new CameraManager.AvailabilityCallback() {
            @Override
            public void onCameraAvailable(String cameraId) {
                super.onCameraAvailable(cameraId);
                if (sharedPreferenceManager.isCameraEnabled()) {
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
                if (sharedPreferenceManager.isCameraEnabled()) {
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

    // Audio manager callbacks
    @RequiresApi(api = Build.VERSION_CODES.N)
    private AudioManager.AudioRecordingCallback getMicCallback() {
        micCallback = new AudioManager.AudioRecordingCallback() {
            @Override
            public void onRecordingConfigChanged(List<AudioRecordingConfiguration> configs) {
                if (sharedPreferenceManager.isMicEnabled()) {
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


    // Location service Callbacks
    private LocationListener locationListener = location -> {
        if (Constants.isDebug()) {
            Log.i(TAG, getString(R.string.dotservice_location) + location.toString());
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    private final GnssStatus.Callback locationCallback = new GnssStatus.Callback() {
        @Override
        public void onStarted() {
            super.onStarted();
            if (sharedPreferenceManager.isLocationEnabled()) {
                didLocUseStart = true;
                isLocUnavailable = true;
                showLocDot();
                triggerVibration();
                makeLog();
                showOnUseNotification();
            }

        }

        @Override
        public void onStopped() {
            super.onStopped();
            if (sharedPreferenceManager.isLocationEnabled()) {
                hideLocDot();
                isLocUnavailable = false;
                didLocUseStart = true;
                makeLog();
                dismissOnUseNotification();
            }

        }
    };


    /*
     * Notification alert when app is accessing your privacy sensors
     * Takes in the variable of app in last accessibility event
     */
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
     * Generates the notification title
     */
    private String getNotificationTitle() {
        String title = getString(R.string.dotservice_monitored_your);
        if (isCameraUnavailable) {
            title = title + getString(R.string.dotservice_monitored_camera);
        }
        if (isMicUnavailable) {
            title = title + getString(R.string.dotservice_monitored_mic);
        }
        if (isLocUnavailable) {
            title = title + getString(R.string.dotservice_monitored_location);
        }
        return title + getString(R.string.dotservice_monitored);
    }

    /**
     * Generates the notification description
     * Takes in the currently running application name from last accessibility event
     */
    private String getNotificationDescription(String appUsingComponent) {
        if (appUsingComponent.isEmpty() || appUsingComponent.equals(getString(R.string.dotservice_unknown))) {
            appUsingComponent = getString(R.string.dotservice_someapp);
        }
        String description = appUsingComponent + getString(R.string.dotservice_isusingyour);
        if (isCameraUnavailable) {
            description = description + getString(R.string.dotservice_monitored_camera);
        }
        if (isMicUnavailable) {
            description = description + getString(R.string.dotservice_monitored_mic);
        }
        if (isLocUnavailable) {
            description = description + getString(R.string.dotservice_monitored_location);
        }
        return description;
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

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        return PendingIntent.getActivity(getApplicationContext(), 1, intent, FLAG_UPDATE_CURRENT);
    }


    /*
     * Method to make log to the room database
     *
     */
    private void makeLog() {
        int cameraState = 0;
        int micState = 0;
        int locState = 0;

        // sets the state of camera
        if (didCameraUseStart && isCameraUnavailable) {
            cameraState = 1;
        } else {
            if (didCameraUseStart && !isCameraUnavailable) {
                cameraState = 2;
                didCameraUseStart = false;
            }
        }

        // sets the state of mic
        if (didMicUseStart && isMicUnavailable) {
            micState = 1;
        } else {
            if (didMicUseStart && !isMicUnavailable) {
                micState = 2;
                didMicUseStart = false;
            }
        }

        // sets the state of location
        if (didLocUseStart && isLocUnavailable) {
            locState = 1;
        } else {
            if (didLocUseStart && !isLocUnavailable) {
                locState = 2;
                didLocUseStart = false;
            }
        }


        if (!currentRunningAppPackage.equals(BuildConfig.APPLICATION_ID)) {
            Logs log = new Logs(System.currentTimeMillis(), currentRunningAppPackage, cameraState, micState, locState);
            mLogsRepository.insertLog(log);
        }
    }


    // CUSTOMISATION SETTINGS

    private void setDotCustomColors() {
        setViewTint(dotCamera, sharedPreferenceManager.getCameraDotColor());
        setViewTint(dotMic, sharedPreferenceManager.getMicDotColor());
        setViewTint(dotLoc, sharedPreferenceManager.getLocationDotColor());
    }

    private void setViewTint(ImageView imageView, int color) {
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_dot);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        Log.i(TAG, "setViewTint: camera color " + color);
        imageView.setBackground(drawable);
    }

    /**
     * For android versions below oreo gives a vibration of 500ms
     * For O above with effect - DEFAULT_AMPLITUDE
     * for Q above with effect - EFFECT_HEAVY_CLICK
     * todo make vibration pattern
     */
    private void triggerVibration() {

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setFlags(FLAG_IMMUTABLE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        if (sharedPreferenceManager.isVibrationEnabled()) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.EFFECT_HEAVY_CLICK));
            } else {
                v.vibrate(500);

            }
        }
    }

    private void getIconsEnabled() {
        if (sharedPreferenceManager.isIconsEnabled()) {
            dotCamera.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_round_camera));
            dotMic.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_round_mic));
            dotLoc.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_round_location));
        } else {
            dotCamera.setImageDrawable(null);
            dotMic.setImageDrawable(null);
            dotLoc.setImageDrawable(null);
        }
    }


    /**
     * get's alignment of dots
     *
     * @return
     */
    private int getLayoutGravity() {
        int position = sharedPreferenceManager.getDotPosition();
        switch (position) {
            case 0:
                // left
                return Gravity.TOP | Gravity.START;
            case 1:
                //right
                return Gravity.TOP | Gravity.END;
            case 2:
                // center
                return Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            default:
                // right on default
                return Gravity.TOP | Gravity.END;
        }
    }


    /// show dots functions ---------------------------

    private void showCamDot() {
        if (sharedPreferenceManager.isCameraEnabled()) {
            updateLayoutGravity();
            setDotCustomColors();
            getIconsEnabled();
            upScaleView(dotCamera);
            dotCamera.setVisibility(View.VISIBLE);
        }
    }

    private void showMicDot() {
        if (sharedPreferenceManager.isMicEnabled()) {
            updateLayoutGravity();
            setDotCustomColors();
            getIconsEnabled();
            upScaleView(dotMic);
            dotMic.setVisibility(View.VISIBLE);
        }
    }


    private void showLocDot() {
        if (sharedPreferenceManager.isLocationEnabled()) {
            updateLayoutGravity();
            setDotCustomColors();
            getIconsEnabled();
            upScaleView(dotLoc);
            dotLoc.setVisibility(View.VISIBLE);
        }
    }

    /// hide dots functions ---------------------------

    private void hideMicDot() {
        downScaleView(dotMic);
        dotMic.setVisibility(View.GONE);
    }

    private void hideCamDot() {
        downScaleView(dotCamera);
        dotCamera.setVisibility(View.GONE);
    }


    private void hideLocDot() {
        downScaleView(dotLoc);
        dotLoc.setVisibility(View.GONE);
    }


    // Dot animations

    public void upScaleView(View view) {
        view.animate().scaleX(1f).scaleY(1f).setDuration(500);
    }

    public void downScaleView(View view) {
        view.animate().scaleX(0f).scaleY(0f).setDuration(500);
    }


    // Initialise the dots
    private void initDotViews() {
        dotCamera = hoverLayout.findViewById(R.id.dot_camera);
        dotMic = hoverLayout.findViewById(R.id.dot_mic);
        dotLoc = hoverLayout.findViewById(R.id.dot_location);
        setDotCustomColors();

        dotCamera.postDelayed(() -> {
            dotCamera.setVisibility(View.GONE);
            dotMic.setVisibility(View.GONE);
            dotLoc.setVisibility(View.GONE);
        }, 300);

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


    @Override
    public void onInterrupt() {
        if (Constants.isDebug()) {
            Log.i(TAG, "onInterrupt: ");
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        try {
            if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && accessibilityEvent.getPackageName() != null) {
                ComponentName componentName = new ComponentName(accessibilityEvent.getPackageName().toString(), accessibilityEvent.getClassName().toString());
                currentRunningAppPackage = componentName.getPackageName();
                currentLivePackage.setValue(componentName.getPackageName());
            }
        } catch (Exception ignored) {
            if (Constants.isDebug()) {
                Log.i(TAG, "onAccessibilityEvent:" + new Exception(ignored).getMessage());
            }
        }

    }


    @Override
    public void onDestroy() {
        unRegisterCameraCallBack();
        unRegisterMicCallback();
        unRegisterLocCallback();

        if (notificationManager != null) {
            notificationManager.cancel(3);
        }
        // we cannot remove accessibility service
        stopForeground(true);
        super.onDestroy();

    }


    // Unregistering on destroy
    private void unRegisterCameraCallBack() {
        if (cameraManager != null && cameraCallback != null) {
            cameraManager.unregisterAvailabilityCallback(cameraCallback);
        }
    }

    private void unRegisterMicCallback() {
        if (audioManager != null && micCallback != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                audioManager.unregisterAudioRecordingCallback(micCallback);
            }
        }
    }

    private void unRegisterLocCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (locationManager != null) {
                locationManager.unregisterGnssStatusCallback(locationCallback);
            }
        }
    }


}