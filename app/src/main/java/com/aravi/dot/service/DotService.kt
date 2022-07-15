package com.aravi.dot.service

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraManager.AvailabilityCallback
import android.location.GnssStatus
import android.location.LocationManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioManager.AudioRecordingCallback
import android.media.AudioRecordingConfiguration
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
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
import java.util.*

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

    private var calendar = Calendar.getInstance()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
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
                .setContentTitle("SafeDot is running in the background")
                .setContentText("Click here to hide notification")
                .setSmallIcon(R.drawable.transparent)
                .setContentIntent(pendingIntent)
                .setTicker("Protect service")
                .build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                startForeground(
                    3, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                            or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                            or ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(3, notification)
            }
        }
    }

    override fun onServiceConnected() {
        sendBroadcast(Intent(KEY_ACCESSIBILITY_START).setPackage(BuildConfig.APPLICATION_ID))
        sharedPreferenceManager = PreferenceManager(this)
        notificationManager = NotificationManagerCompat.from(applicationContext)
        initDotViews()
        initHardwareCallbacks()
    }

    private fun initDotViews() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        hoverLayout = FrameLayout(this)
        layoutParams = WindowManager.LayoutParams()

        val inflater = LayoutInflater.from(this)

        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        layoutParams.format = PixelFormat.TRANSLUCENT
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = layoutGravity

        inflater.inflate(R.layout.dot_layout, hoverLayout)
        windowManager.addView(hoverLayout, layoutParams)
        dotCamera = hoverLayout.findViewById(R.id.dot_camera)
        dotMic = hoverLayout.findViewById(R.id.dot_mic)
        dotLoc = hoverLayout.findViewById(R.id.dot_location)
        setDotCustomColors()

        dotCamera.postDelayed({
            dotCamera.visibility = View.GONE
            dotMic.visibility = View.GONE
            dotLoc.visibility = View.GONE
        }, if (isDebug) 1000 else 10) // to check if dots are initialised
    }


    private fun initHardwareCallbacks() {
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        cameraManager.registerAvailabilityCallback(getCameraCallback(), null)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.registerAudioRecordingCallback(getMicCallback(), null)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (checkLocationPermission()) {
            sharedPreferenceManager.isLocationEnabled = true
            locationManager.registerGnssStatusCallback(locationCallback)
        } else {
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
                if (sharedPreferenceManager.isCameraEnabled) {
                    isCameraUnavailable = false
                    didCameraUseStart = true
                    dismissOnUseNotification()
                    hideCamDot()
                    recordLog(PERMISSION_CAMERA)
                }
            }

            override fun onCameraUnavailable(cameraId: String) {
                super.onCameraUnavailable(cameraId)
                if (sharedPreferenceManager.isCameraEnabled) {
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
                if (sharedPreferenceManager.isMicEnabled) {
                    if (configs.isNotEmpty()) {
                        showMicDot()
                        triggerVibration()
                        isMicUnavailable = true
                        showOnUseNotification()
                        recordLog(PERMISSION_MICROPHONE)

                    } else {
                        hideMicDot()
                        isMicUnavailable = false
                        dismissOnUseNotification()
                        recordLog(PERMISSION_MICROPHONE)

                    }
                    didMicUseStart = true
                }
            }
        }
        return micCallback
    }

    // Location service Callbacks
    private val locationCallback: GnssStatus.Callback = object : GnssStatus.Callback() {
        override fun onStarted() {
            if (sharedPreferenceManager.isLocationEnabled) {
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
            if (sharedPreferenceManager.isLocationEnabled) {
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
        if (currentRunningAppPackage == BuildConfig.APPLICATION_ID) return // avoid self logs
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
            val log = Log(
                System.currentTimeMillis(),
                currentRunningAppPackage,
                permission,
                state,
                utils.getDateFromTimestamp(calendar.timeInMillis)
            )
            appDatabase.logsDao().insertLog(log)
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
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
            var title = "Your "
            if (isCameraUnavailable) {
                title += "CAMERA, "
            }
            if (isMicUnavailable) {
                title += "MIC, "
            }
            if (isLocUnavailable) {
                title += "LOCATION"
            }
            return "$title are being monitored"
        }


    private fun getNotificationDescription(appUsingComponent: String): String {
        var appUsingComponent = appUsingComponent
        if (appUsingComponent.isEmpty() || appUsingComponent == "(unknown)") {
            appUsingComponent = "some app"
        }
        var description = "$appUsingComponent is using your "
        if (isCameraUnavailable) {
            description += "CAMERA, "
        }
        if (isMicUnavailable) {
            description += "MIC, "
        }
        if (isLocUnavailable) description += "LOCATION"
        return description
    }

    private fun showOnUseNotification() {
        if (isOnUseNotificationEnabled) {
            initOnUseNotification(utils.getNameFromPackageName(currentRunningAppPackage))
            notificationManager.notify(
                NOTIFICATION_ID,
                notificationCompatBuilder!!.build()
            )
        }
    }

    private fun dismissOnUseNotification() {
        if (isCameraUnavailable || isMicUnavailable) {
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
        val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.ic_dot)
        drawable!!.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        imageView!!.background = drawable
    }

    private fun triggerVibration() {
        val audioAttributes = AudioAttributes.Builder()
            .setFlags(PendingIntent.FLAG_IMMUTABLE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        if (sharedPreferenceManager.isVibrationEnabled) {
            val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                v.vibrate(500)
            }
        }
    }

    private val iconsEnabled: Unit
        get() {
            if (sharedPreferenceManager.isIconsEnabled) {
                dotCamera.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_round_camera
                    )
                )
                dotMic.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_round_mic
                    )
                )
                dotLoc.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_round_location
                    )
                )
            } else {
                dotCamera.setImageDrawable(null)
                dotMic.setImageDrawable(null)
                dotLoc.setImageDrawable(null)
            }
        }// right on default// center//right

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
            iconsEnabled
            upScaleView(dotCamera)
            dotCamera.visibility = View.VISIBLE
        }
    }

    private fun showMicDot() {
        if (sharedPreferenceManager.isMicEnabled) {
            updateLayoutGravity()
            setDotCustomColors()
            iconsEnabled
            upScaleView(dotMic)
            dotMic.visibility = View.VISIBLE
        }
    }

    private fun showLocDot() {
        if (sharedPreferenceManager.isLocationEnabled) {
            updateLayoutGravity()
            setDotCustomColors()
            iconsEnabled
            upScaleView(dotLoc)
            dotLoc.visibility = View.VISIBLE
        }
    }

    /// hide dots functions ---------------------------
    private fun hideMicDot() {
        downScaleView(dotMic)
        dotMic.visibility = View.GONE
    }

    private fun hideCamDot() {
        downScaleView(dotCamera)
        dotCamera.visibility = View.GONE
    }

    private fun hideLocDot() {
        downScaleView(dotLoc)
        dotLoc.visibility = View.GONE
    }

    // Dot animations
    fun upScaleView(view: View?) {
        view!!.animate().scaleX(1f).scaleY(1f).duration = 500
    }

    fun downScaleView(view: View?) {
        view!!.animate().scaleX(0f).scaleY(0f).duration = 500
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

    // Unregistering on destroy
    private fun unRegisterCameraCallBack() {
        cameraManager.unregisterAvailabilityCallback(cameraCallback)
    }

    private fun unRegisterMicCallback() {
        audioManager.unregisterAudioRecordingCallback(micCallback)
    }

    private fun unRegisterLocCallback() {
        locationManager.unregisterGnssStatusCallback(locationCallback)
    }

    override fun onDestroy() {
        unRegisterCameraCallBack()
        unRegisterMicCallback()
        unRegisterLocCallback()
        notificationManager.cancel(3)
        // we cannot remove accessibility service
        stopForeground(true)
        super.onDestroy()
    }


}
