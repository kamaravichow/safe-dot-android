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

package com.aravi.dot.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Utils {

    /**
     * @param input
     * @return
     */
    public static String capitalizeFirstLetterOfString(String input) {
        if (input.isEmpty()) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    /**
     * @param context
     */
    public static void showAutoStartDialog(Context context, String manufacturer) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Enable AutoStart ")
                .setMessage(manufacturer.toUpperCase() + " devices will kill the useful services to free up ram. You're required to provide the auto start permission to the app to keep app running as expected. ")
                .setPositiveButton("Setup Now", (dialog, which) -> {
                    openAutoStartAccordingToManufacturer(context);
                })
                .setCancelable(true)
                .show();
    }

    /**
     * @param context
     */
    private static void openAutoStartAccordingToManufacturer(Context context) {
        try {
            Intent intent = new Intent();
            String manufacturer = android.os.Build.MANUFACTURER;
            if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
            } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
            } else if ("Honor".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
            }

            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (list.size() > 0) {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            android.util.Log.i("UTILS", "openAutoStartAccordingToManufacturer: " + e.getMessage());
        }
    }


    /**
     * @param context
     * @param packageName
     * @return
     */
    public static String getNameFromPackageName(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "(unknown)");
    }

    /**
     * @param millis
     * @return
     */
    public static String convertSecondsToHMmSs(long millis) {
        Date date = new Date(millis);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(cal.getTimeZone());
        return formatter.format(date);
    }
}
