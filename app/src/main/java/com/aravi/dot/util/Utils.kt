/*
 * Copyright (C) 2021.  Aravind Chowdary (@kamaravichow)
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
package com.aravi.dot.util

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import android.util.Log
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.aravi.dot.BuildConfig
import com.aravi.dot.Constants
import com.aravi.dot.R
import com.aravi.dot.activities.log.LogsActivity
import com.aravi.dot.activities.main.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.core.component.KoinComponent
import java.util.*


class Utils(val context: Context) : KoinComponent {

    fun openHistoryActivity(context: Context, permission: String) {
        val i = Intent(context, LogsActivity::class.java)
        i.putExtra(Constants.EXTRA_PERMISSION, permission)
//
//        val bundle = ActivityOptions
//            .makeCustomAnimation(context, R.anim.slide_out_left, R.anim.slide_in_left)
//            .toBundle()
        context.startActivity(i)
    }

    fun openAppInfoActivity(context: Context, packageName: String?) {
        val bundle = ActivityOptions.makeCustomAnimation(
            context,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        ).toBundle()
        val i = Intent(context, MainActivity::class.java)
        i.putExtra(Constants.EXTRA_APP, packageName)
        context.startActivity(i)
    }


    fun openPermissionLog(context: Context, permission: String?) {
        val bundle = ActivityOptions.makeCustomAnimation(
            context,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        ).toBundle()
        val i = Intent(context, LogsActivity::class.java)
        i.putExtra(Constants.EXTRA_PERMISSION, permission)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(i)
    }

    fun openAppSettings(context: Context, packageName: String) {
        val bundle = ActivityOptions.makeCustomAnimation(
            context,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        ).toBundle()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        context.startActivity(intent)
    }

    fun openPrivacySettings(context: Context) {
        val bundle = ActivityOptions.makeCustomAnimation(
            context,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        ).toBundle()
        val intent: Intent
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            intent = Intent(Settings.ACTION_PRIVACY_SETTINGS)
        } else {
            intent = Intent(Intent.ACTION_MAIN)
                .setComponent(
                    ComponentName(
                        "com.android.settings",
                        "com.android.settings.Settings\$AppAndNotificationDashboardActivity"
                    )
                )
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            try {
                context.startActivity(intent, bundle)
            } catch (ignored: java.lang.Exception) {
                context.startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
    }

    fun openLink(url: String?) {
        val bundle = ActivityOptions.makeCustomAnimation(
            context,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        ).toBundle()
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        context.startActivity(i, bundle)
    }

    fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("content://")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(""))
        intent.putExtra(
            Intent.EXTRA_SUBJECT,
            context.getString(R.string.app_name) + ":" + BuildConfig.VERSION_NAME
        )
        intent.putExtra(Intent.EXTRA_TEXT, "")
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            println(e.toString())
        }
    }


    fun getAttrColor(attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return ContextCompat.getColor(context, typedValue.resourceId)
    }

    fun getAttrColor(ctx: Context, attr: Int): Int {
        val typedValue = TypedValue()
        ctx.theme.resolveAttribute(attr, typedValue, true)
        return ContextCompat.getColor(ctx, typedValue.resourceId)
    }

    fun getTimeFromTimestamp(timestamp: Long): String {
        return if (DateFormat.is24HourFormat(context)) {
            DateFormat.format("HH:mm", Date(timestamp)).toString()
        } else {
            DateFormat.format("hh:mm a", Date(timestamp)).toString()
        }
    }

    fun getDateFromTimestamp(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        val today = DateFormat.format("dd MMM", calendar.timeInMillis).toString()
        calendar.add(Calendar.DATE, -1)
        val yesterday = DateFormat.format("dd MMM", calendar.timeInMillis).toString()
        val date = DateFormat.format("dd MMM", Date(timestamp)).toString()
        if (today == date) {
            return context.getString(R.string.log_today)
        }
        return if (yesterday == date) {
            context.getString(R.string.log_yesterday)
        } else date
    }

//    fun getDateFromTimestamp(timestamp: Long): String {
//        return DateFormat.format("dd-MMM-yyyy", Date(timestamp)).toString()
//    }


    fun getAppIcon(packageName: String?): Bitmap {
        val drawable = getIconFromPackageName(packageName)
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun getIconFromPackageName(packageName: String?): Drawable? {
        try {
            return context.packageManager.getApplicationIcon(
                packageName!!
            )
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ResourcesCompat.getDrawable(context.resources, R.mipmap.ic_launcher, context.theme)
    }

    fun showAutoStartDialog(manufacturer: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Enable AutoStart ")
            .setMessage(manufacturer.toUpperCase() + " devices will kill the useful services to free up ram. You're required to provide the auto start permission to the app to keep app running as expected. ")
            .setPositiveButton("Setup Now") { dialog: DialogInterface?, which: Int ->
                openAutoStartAccordingToManufacturer()
            }
            .setCancelable(true)
            .show()
    }

    private fun openAutoStartAccordingToManufacturer() {
        try {
            val intent = Intent()
            val manufacturer = Build.MANUFACTURER
            if ("xiaomi".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            } else if ("oppo".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
            } else if ("vivo".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            } else if ("Honor".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity"
                )
            }
            val list = context.packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            if (list.size > 0) {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.i("UTILS", "openAutoStartAccordingToManufacturer: " + e.message)
        }
    }

    fun getNameFromPackageName(packageName: String): String {
        val packageManager = context.packageManager
        val applicationInfo: ApplicationInfo? = try {
            packageManager.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        return (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "(unknown)") as String
    }


    fun getSystemApps(): List<String> {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo =
            context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val helpPackages: MutableList<String> = ArrayList()
        helpPackages.add("com.android.systemui")
        helpPackages.add("com.android.settings")
        if (resolveInfo != null) {
            helpPackages.add(resolveInfo.activityInfo.packageName)
        }
        return helpPackages
    }


}
