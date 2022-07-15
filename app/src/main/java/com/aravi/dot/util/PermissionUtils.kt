package com.aravi.dot.util

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils.SimpleStringSplitter
import android.view.LayoutInflater
import android.view.accessibility.AccessibilityManager
import androidx.core.app.ActivityCompat
import com.aravi.dot.Constants
import com.aravi.dot.R
import com.aravi.dot.manager.PreferenceManager
import org.koin.core.component.KoinComponent


class PermissionUtils(val context: Context, val utils: Utils) : KoinComponent {
    fun getString(position: Int): String {
        return when (position) {
            Constants.POSITION_LOCATION -> context.getString(R.string.permission_location)
            Constants.POSITION_CAMERA -> context.getString(R.string.permission_camera)
            Constants.POSITION_MICROPHONE -> context.getString(R.string.permission_microphone)
            else -> context.getString(R.string.permission_microphone)
        }
    }

    fun getName(permission: String?): String {
        return when (permission) {
            Constants.PERMISSION_LOCATION -> context.getString(R.string.permission_location)
            Constants.PERMISSION_CAMERA -> context.getString(R.string.permission_camera)
            Constants.PERMISSION_MICROPHONE -> context.getString(R.string.permission_microphone)
            else -> context.getString(R.string.permission_microphone)
        }
    }

    fun getIcon(context: Context?, permission: String?): Int {
        return when (permission) {
            Constants.PERMISSION_LOCATION -> R.drawable.ic_round_location
            Constants.PERMISSION_CAMERA -> R.drawable.ic_round_camera
            Constants.PERMISSION_MICROPHONE -> R.drawable.ic_round_mic
            else -> R.drawable.ic_round_mic
        }
    }

    fun getPermissionUsageInfo(apps: Int): String {
        return if (apps != 0) {
            context.getString(R.string.permission_info)
                .replace("#ALIAS#", apps.toString())
        } else {
            context.getString(R.string.permission_no_apps)
        }
    }

    fun getColor(permission: String): Int {
        return when (permission) {
            Constants.PERMISSION_LOCATION -> utils.getAttrColor(R.attr.colorLocation)
            Constants.PERMISSION_CAMERA -> utils.getAttrColor(R.attr.colorCamera)
            Constants.PERMISSION_MICROPHONE -> utils.getAttrColor(R.attr.colorMicrophone)
            else -> utils.getAttrColor(R.attr.colorMicrophone)
        }
    }

    fun getColor(position: Int): Int {
        return when (position) {
            Constants.POSITION_LOCATION -> R.attr.colorLocation
            Constants.POSITION_CAMERA -> R.attr.colorCamera
            Constants.POSITION_MICROPHONE -> R.attr.colorMicrophone
            else -> R.attr.colorMicrophone
        }
    }

    fun accessibilityPermission(cls: Class<*>?): Boolean {
        val componentName = ComponentName(context, cls!!)
        val string: String = Settings.Secure.getString(
            context.contentResolver,
            "enabled_accessibility_services"
        )
            ?: return false
        val simpleStringSplitter = SimpleStringSplitter(':')
        simpleStringSplitter.setString(string)
        while (simpleStringSplitter.hasNext()) {
            val unflattenFromString = ComponentName.unflattenFromString(simpleStringSplitter.next())
            if (unflattenFromString != null && unflattenFromString == componentName) {
                return true
            }
        }
        return false
    }

    fun checkAccessibility(context: Context): Boolean {
        val manager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return manager.isEnabled
    }

    fun checkLocation(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    fun checkAutoStartRequirement(
        context: Context?,
        inflater: LayoutInflater?,
        preferenceManager: PreferenceManager
    ) {
        val manufacturer = Build.MANUFACTURER
//        if (preferenceManager.isFirstLaunch()) {
//            if ("xiaomi".equals(manufacturer, ignoreCase = true)
//                || "oppo".equals(manufacturer, ignoreCase = true)
//                || "vivo".equals(manufacturer, ignoreCase = true)
//                || "Honor".equals(manufacturer, ignoreCase = true)
//            ) {
//                showAutoStartDialog(context, inflater)
//                preferenceManager.setFirstLaunch(false)
//            }
    }
}
