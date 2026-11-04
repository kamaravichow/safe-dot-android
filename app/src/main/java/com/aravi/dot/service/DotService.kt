package com.aravi.dot.service

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraManager.AvailabilityCallback
import android.location.GnssStatus
import android.location.LocationManager
import android.media.AudioManager
import android.media.AudioManager.AudioRecordingCallback
import android.media.AudioRecordingConfiguration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.aravi.dot.manager.PreferenceManager.PREF_CONSTANTS
import com.aravi.dot.BuildConfig
import com.aravi.dot.Constants
import com.aravi.dot.Constants.NOTIFICATION_ID
import com.aravi.dot.Constants.PERMISSION_CAMERA
import com.aravi.dot.Constants.PERMISSION_LOCATION
import com.aravi.dot.Constants.PERMISSION_MICROPHONE
import com.aravi.dot.Constants.isDebug
import com.aravi.dot.R
import com.aravi.dot.activities.main.MainActivity
import com.aravi.dot.bean.Log
import com.aravi.dot.database.AppDatabase
import com.aravi.dot.manager.PreferenceManager
import com.aravi.dot.util.PermissionUtils
import com.aravi.dot.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class DotService : AccessibilityService() {
    companion object {
        val TAG = DotService::class.java.simpleName
        const val KEY_ACCESSIBILITY_START = "accessibility_start"
    }

    private val utils: Utils by inject()
    private val permissionUtils: PermissionUtils by inject()

    private val appDatabase: AppDatabase by inject()

    private var isCameraUnavailable = false
    private var isMicUnavailable = false
    private var isLocUnavailable = false
    private val isOnUseNotificationEnabled = true

    private var didCameraUseStart = false
    private var didMicUseStart = false
    private var didLocUseStart = false
    private val activeCameras = mutableSetOf<String>()
    private var locationCallbackRegistered = false

    private lateinit var hoverLayout: FrameLayout
    private lateinit var dotCamera: ImageView
    private lateinit var dotMic: ImageView
    private lateinit var dotLoc: ImageView

    private lateinit var cameraManager: CameraManager
    private lateinit var cameraCallback: AvailabilityCallback
    private lateinit var audioManager: AudioManager
    private lateinit var micCallback: AudioRecordingCallback
    private lateinit var locationManager: LocationManager

    private lateinit var sharedPreferenceManager: PreferenceManager
    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams

    private lateinit var notificationManager: NotificationManagerCompat
    private var notificationCompatBuilder: NotificationCompat.Builder? = null
    private var currentRunningAppPackage = BuildConfig.APPLICATION_ID

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationIntent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID)
                .putExtra(Settings.EXTRA_CHANNEL_ID, Constants.SERVICE_NOTIFICATION_CHANNEL)
            val pendingIntent = PendingIntent.getActivity(
                this,
                24,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            val notification = Notification.Builder(this, Constants.SERVICE_NOTIFICATION_CHANNEL)
                .setContentTitle(getString(R.string.notification_ContentText))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(R.drawable.transparent)
                .setContentIntent(pendingIntent)
                .setTicker(getString(R.string.notification_ticker))
                .build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    3,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(3, notification)
            }
        }
    }

    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                PREF_CONSTANTS.LOCATION_KEY -> syncLocationMonitoring()
                PREF_CONSTANTS.CAMERA_KEY -> syncCameraIndicator()
                PREF_CONSTANTS.MIC_KEY -> syncMicIndicator()
                PREF_CONSTANTS.CAMERA_DOT_COLOR,
                PREF_CONSTANTS.MIC_DOT_COLOR,
                PREF_CONSTANTS.LOC_DOT_COLOR,
                PREF_CONSTANTS.ICON_KEY,
                PREF_CONSTANTS.PLACEMENT_KEY -> {
                    if (this::dotCamera.isInitialized) {
                        setDotCustomColors()
                        updateIconsVisibility()
                        updateLayoutGravity()
                    }
                }
            }
        }

    override fun onServiceConnected() {
        sendBroadcast(Intent(KEY_ACCESSIBILITY_START).setPackage(BuildConfig.APPLICATION_ID))
        sharedPreferenceManager = PreferenceManager(this)
        notificationManager = NotificationManagerCompat.from(applicationContext)
        initDotViews()
        initHardwareCallbacks()
        sharedPreferenceManager.registerListener(preferenceChangeListener)
    }

    private fun initDotViews() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        hoverLayout = FrameLayout(this)
        layoutParams = WindowManager.LayoutParams()

        val inflater = LayoutInflater.from(this)

        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        layoutParams.format = PixelFormat.TRANSLUCENT
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            layoutParams.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutParams.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = layoutGravity

        inflater.inflate(R.layout.dot_layout, hoverLayout)
        windowManager.addView(hoverLayout, layoutParams)
        dotCamera = hoverLayout.findViewById(R.id.dot_camera)
        dotMic = hoverLayout.findViewById(R.id.dot_mic)
        dotLoc = hoverLayout.findViewById(R.id.dot_location)
        setDotCustomColors()

        val hideDots = Runnable {
            dotCamera.visibility = View.GONE
            dotMic.visibility = View.GONE
            dotLoc.visibility = View.GONE
            dotCamera.scaleX = 0f
            dotCamera.scaleY = 0f
            dotMic.scaleX = 0f
            dotMic.scaleY = 0f
            dotLoc.scaleX = 0f
            dotLoc.scaleY = 0f
        }
        if (isDebug) {
            dotCamera.postDelayed(hideDots, 1000) // to check if dots are initialised
        } else {
            hideDots.run()
        }
    }


    private fun initHardwareCallbacks() {
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        cameraManager.registerAvailabilityCallback(getCameraCallback(), null)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.registerAudioRecordingCallback(getMicCallback(), null)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (sharedPreferenceManager.isLocationEnabled && checkLocationPermission()) {
            try {
                registerLocationCallback()
            } catch (e: SecurityException) {
                sharedPreferenceManager.isLocationEnabled = false
            }
        } else if (sharedPreferenceManager.isLocationEnabled) {
            sharedPreferenceManager.isLocationEnabled = false
        }
    }

    private fun registerLocationCallbacks() {

    }


    // Camera service callbacks
    private fun getCameraCallback(): AvailabilityCallback {
        cameraCallback = object : AvailabilityCallback() {
            override fun onCameraAvailable(cameraId: String) {
                super.onCameraAvailable(cameraId)
                val wasRemoved = activeCameras.remove(cameraId)
                if (wasRemoved &&
                    activeCameras.isEmpty() &&
                    sharedPreferenceManager.isCameraEnabled &&
                    isCameraUnavailable
                ) {
                    isCameraUnavailable = false
                    didCameraUseStart = true
                    dismissOnUseNotification()
                    hideCamDot()
                    recordLog(PERMISSION_CAMERA)
                }
            }

            override fun onCameraUnavailable(cameraId: String) {
                super.onCameraUnavailable(cameraId)
                val wasEmpty = activeCameras.isEmpty()
                activeCameras.add(cameraId)
                if (wasEmpty && sharedPreferenceManager.isCameraEnabled) {
                    isCameraUnavailable = true
                    didCameraUseStart = true
                    showOnUseNotification()
                    showCamDot()
                    triggerVibration()
                    recordLog(PERMISSION_CAMERA)
                }
            }
        }
        return cameraCallback
    }

    // Audio manager callbacks
    private fun getMicCallback(): AudioRecordingCallback {
        micCallback = object : AudioRecordingCallback() {
            override fun onRecordingConfigChanged(configs: List<AudioRecordingConfiguration>) {
                if (!sharedPreferenceManager.isMicEnabled) return
                if (configs.isNotEmpty()) {
                    if (!isMicUnavailable) {
                        didMicUseStart = true
                        isMicUnavailable = true
                        showMicDot()
                        triggerVibration()
                        showOnUseNotification()
                        recordLog(PERMISSION_MICROPHONE)
                    }
                } else if (isMicUnavailable) {
                    didMicUseStart = true
                    isMicUnavailable = false
                    hideMicDot()
                    dismissOnUseNotification()
                    recordLog(PERMISSION_MICROPHONE)
                }
            }
        }
        return micCallback
    }

    // Location service Callbacks
    private val locationCallback: GnssStatus.Callback = object : GnssStatus.Callback() {
        override fun onStarted() {
            if (sharedPreferenceManager.isLocationEnabled && !isLocUnavailable) {
                didLocUseStart = true
                isLocUnavailable = true
                showLocDot()
                triggerVibration()
                showOnUseNotification()
                recordLog(PERMISSION_LOCATION)
            }
            super.onStarted()
        }

        override fun onStopped() {
            if (sharedPreferenceManager.isLocationEnabled && isLocUnavailable) {
                hideLocDot()
                isLocUnavailable = false
                didLocUseStart = true
                dismissOnUseNotification()
                recordLog(PERMISSION_LOCATION)
            }
            super.onStopped()
        }
    }


    private fun recordLog(permission: String) {
        if (currentRunningAppPackage == BuildConfig.APPLICATION_ID) return
        if (sharedPreferenceManager.excludedPackages.contains(currentRunningAppPackage)) return
        var state = Constants.STATE_INVALID
        when (permission) {
            PERMISSION_CAMERA -> state =
                if (didCameraUseStart && isCameraUnavailable) Constants.STATE_ON else Constants.STATE_OFF
            PERMISSION_MICROPHONE -> state =
                if (didMicUseStart && isMicUnavailable) Constants.STATE_ON else Constants.STATE_OFF
            PERMISSION_LOCATION -> state =
                if (didLocUseStart && isLocUnavailable) Constants.STATE_ON else Constants.STATE_OFF
        }
        CoroutineScope(Dispatchers.IO).launch {
            val now = System.currentTimeMillis()
            val log = Log(
                now,
                currentRunningAppPackage,
                permission,
                state,
                utils.getLogDayKey(now)
            )
            appDatabase.logsDao().insertLog(log)
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun initOnUseNotification(appUsingComponent: String) {
        notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, Constants.DEFAULT_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.transparent)
                .setContentTitle(notificationTitle)
                .setContentText(getNotificationDescription(appUsingComponent))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
    }


    private val notificationTitle: String
        get() {
            var title = getString(R.string.dotservice_monitored_your)
            if (isCameraUnavailable) {
                title += getString(R.string.dotservice_monitored_camera)
            }
            if (isMicUnavailable) {
                title += getString(R.string.dotservice_monitored_mic)
            }
            if (isLocUnavailable) {
                title += getString(R.string.dotservice_monitored_location)
            }
            return title + getString(R.string.dotservice_monitored)
        }


    private fun getNotificationDescription(appUsingComponent: String): String {
        var appUsingComponent = appUsingComponent
        if (appUsingComponent.isEmpty() || appUsingComponent == getString(R.string.dotservice_unknown)) {
            appUsingComponent = getString(R.string.dotservice_someapp)
        }
        var description = appUsingComponent + getString(R.string.dotservice_isusingyour)
        if (isCameraUnavailable) {
            description += getString(R.string.dotservice_monitored_camera)
        }
        if (isMicUnavailable) {
            description += getString(R.string.dotservice_monitored_mic)
        }
        if (isLocUnavailable) description += getString(R.string.dotservice_monitored_location)
        return description
    }

    private fun showOnUseNotification() {
        if (!isOnUseNotificationEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        initOnUseNotification(utils.getNameFromPackageName(currentRunningAppPackage))
        notificationManager.notify(
            NOTIFICATION_ID,
            notificationCompatBuilder!!.build()
        )
    }

    private fun dismissOnUseNotification() {
        if (isCameraUnavailable || isMicUnavailable || isLocUnavailable) {
            showOnUseNotification()
        } else {
            notificationManager.cancel(NOTIFICATION_ID)
        }
    }

    private val pendingIntent: PendingIntent
        get() {
            val intent = Intent(applicationContext, MainActivity::class.java)
            return PendingIntent.getActivity(
                applicationContext,
                1,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }

    // CUSTOMISATION SETTINGS
    private fun setDotCustomColors() {
        setViewTint(dotCamera, sharedPreferenceManager.cameraDotColor)
        setViewTint(dotMic, sharedPreferenceManager.micDotColor)
        setViewTint(dotLoc, sharedPreferenceManager.locationDotColor)
    }

    private fun setViewTint(imageView: ImageView?, color: Int) {
        val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.ic_dot)?.mutate()
        drawable?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            color,
            BlendModeCompat.SRC_ATOP
        )
        imageView?.background = drawable
    }

    private fun triggerVibration() {
        if (!sharedPreferenceManager.isVibrationEnabled) return
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(150)
        }
    }

    private fun updateIconsVisibility() {
        if (sharedPreferenceManager.isIconsEnabled) {
            dotCamera.setImageDrawable(
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_round_camera)
            )
            dotMic.setImageDrawable(
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_round_mic)
            )
            dotLoc.setImageDrawable(
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_round_location)
            )
        } else {
            dotCamera.setImageDrawable(null)
            dotMic.setImageDrawable(null)
            dotLoc.setImageDrawable(null)
        }
    }

    // left
    private val layoutGravity: Int
        get() {
            return when (sharedPreferenceManager.dotPosition) {
                0 -> Gravity.TOP or Gravity.START
                1 -> Gravity.TOP or Gravity.END
                2 -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
                else -> Gravity.TOP or Gravity.END
            }
        }

    /// show dots functions ---------------------------
    private fun showCamDot() {
        if (sharedPreferenceManager.isCameraEnabled) {
            updateLayoutGravity()
            setDotCustomColors()
            updateIconsVisibility()
            upScaleView(dotCamera)
            dotCamera.visibility = View.VISIBLE
        }
    }

    private fun showMicDot() {
        if (sharedPreferenceManager.isMicEnabled) {
            updateLayoutGravity()
            setDotCustomColors()
            updateIconsVisibility()
            upScaleView(dotMic)
            dotMic.visibility = View.VISIBLE
        }
    }

    private fun showLocDot() {
        if (sharedPreferenceManager.isLocationEnabled) {
            updateLayoutGravity()
            setDotCustomColors()
            updateIconsVisibility()
            upScaleView(dotLoc)
            dotLoc.visibility = View.VISIBLE
        }
    }

    /// hide dots functions ---------------------------
    private fun hideMicDot() {
        downScaleView(dotMic)
    }

    private fun hideCamDot() {
        downScaleView(dotCamera)
    }

    private fun hideLocDot() {
        downScaleView(dotLoc)
    }

    // Dot animations
    fun upScaleView(view: View?) {
        if (view == null) return
        view.animate().cancel()
        view.visibility = View.VISIBLE
        view.animate().scaleX(1f).scaleY(1f).setDuration(500).withEndAction(null).start()
    }

    fun downScaleView(view: View?) {
        if (view == null) return
        view.animate().cancel()
        view.animate().scaleX(0f).scaleY(0f).setDuration(500).withEndAction {
            view.visibility = View.GONE
        }.start()
    }

    // Initialise the dots


    private fun updateLayoutGravity() {
        layoutParams.gravity = layoutGravity
        windowManager.updateViewLayout(hoverLayout, layoutParams)
    }

    override fun onInterrupt() {}
    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent) {
        try {
            if (accessibilityEvent.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && accessibilityEvent.packageName != null) {
                val componentName = ComponentName(
                    accessibilityEvent.packageName.toString(),
                    accessibilityEvent.className.toString()
                )
                currentRunningAppPackage = componentName.packageName
            }
        } catch (ignored: Exception) {
        }
    }

    private fun syncLocationMonitoring() {
        if (!this::locationManager.isInitialized) return
        if (sharedPreferenceManager.isLocationEnabled && checkLocationPermission()) {
            try {
                registerLocationCallback()
            } catch (e: SecurityException) {
                sharedPreferenceManager.isLocationEnabled = false
            }
        } else {
            if (sharedPreferenceManager.isLocationEnabled) {
                sharedPreferenceManager.isLocationEnabled = false
            }
            unregisterLocationCallback()
            if (isLocUnavailable) {
                isLocUnavailable = false
                didLocUseStart = true
                hideLocDot()
                dismissOnUseNotification()
                recordLog(PERMISSION_LOCATION)
            }
        }
    }

    private fun syncCameraIndicator() {
        if (sharedPreferenceManager.isCameraEnabled && activeCameras.isNotEmpty()) {
            if (!isCameraUnavailable) {
                isCameraUnavailable = true
                didCameraUseStart = true
                showCamDot()
                showOnUseNotification()
                recordLog(PERMISSION_CAMERA)
            }
        } else if (!sharedPreferenceManager.isCameraEnabled && isCameraUnavailable) {
            isCameraUnavailable = false
            didCameraUseStart = true
            hideCamDot()
            dismissOnUseNotification()
            recordLog(PERMISSION_CAMERA)
        }
    }

    private fun syncMicIndicator() {
        if (!this::audioManager.isInitialized) return
        val recording = audioManager.activeRecordingConfigurations.isNotEmpty()
        if (sharedPreferenceManager.isMicEnabled && recording) {
            if (!isMicUnavailable) {
                isMicUnavailable = true
                didMicUseStart = true
                showMicDot()
                showOnUseNotification()
                recordLog(PERMISSION_MICROPHONE)
            }
        } else if (!sharedPreferenceManager.isMicEnabled && isMicUnavailable) {
            isMicUnavailable = false
            didMicUseStart = true
            hideMicDot()
            dismissOnUseNotification()
            recordLog(PERMISSION_MICROPHONE)
        }
    }

    private fun registerLocationCallback() {
        if (locationCallbackRegistered || !this::locationManager.isInitialized) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.registerGnssStatusCallback(mainExecutor, locationCallback)
        } else {
            @Suppress("DEPRECATION")
            locationManager.registerGnssStatusCallback(
                locationCallback,
                Handler(Looper.getMainLooper())
            )
        }
        locationCallbackRegistered = true
    }

    private fun unregisterLocationCallback() {
        if (!locationCallbackRegistered || !this::locationManager.isInitialized) return
        locationManager.unregisterGnssStatusCallback(locationCallback)
        locationCallbackRegistered = false
    }

    // Unregistering on destroy
    private fun unRegisterCameraCallBack() {
        if (this::cameraManager.isInitialized && this::cameraCallback.isInitialized) {
            cameraManager.unregisterAvailabilityCallback(cameraCallback)
        }
    }

    private fun unRegisterMicCallback() {
        if (this::audioManager.isInitialized && this::micCallback.isInitialized) {
            audioManager.unregisterAudioRecordingCallback(micCallback)
        }
    }

    override fun onDestroy() {
        if (this::sharedPreferenceManager.isInitialized) {
            sharedPreferenceManager.unregisterListener(preferenceChangeListener)
        }
        unRegisterCameraCallBack()
        unRegisterMicCallback()
        unregisterLocationCallback()
        if (this::notificationManager.isInitialized) {
            notificationManager.cancel(3)
            notificationManager.cancel(NOTIFICATION_ID)
        }
        if (this::windowManager.isInitialized && this::hoverLayout.isInitialized) {
            try {
                windowManager.removeView(hoverLayout)
            } catch (e: Exception) {
                // The overlay was already removed, or its window token is no longer valid.
            }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }


}
