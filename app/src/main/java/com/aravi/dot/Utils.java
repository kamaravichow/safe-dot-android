package com.aravi.dot;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;

import com.aravi.dot.manager.SharedPreferenceManager;
import com.aravi.dot.model.Log;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Utils {

    public static void showAutoStartDialog(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Enable AutoStart")
                .setMessage("You're required to provide the auto start permission to the app to keep app running as expected. ")
                .setPositiveButton("Setup Now", (dialog, which) -> {
                    openAutoStartAccordingToManufacturer(context);
                })
                .setCancelable(true)
                .show();
    }

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

    public static void createNewLog(Context context, String packageName, int camera, int mic) {
        if (packageName.equals("com.aravi.dot")) {
            return;
        }
        AsyncTask.execute(() -> {
            SharedPreferences preferences = context.getSharedPreferences("APP.USAGE.LOG", Context.MODE_PRIVATE);
            Gson gson = new Gson();
            List<Log> logList = new ArrayList<>();
            List<Log> savedLog;

            Type logListType = new TypeToken<ArrayList<Log>>() {
            }.getType();
            savedLog = gson.fromJson(preferences.getString("LOG.USAGE", null), logListType);
            if (savedLog != null && !savedLog.isEmpty()) {
                logList.addAll(savedLog);
            }

            Log log = new Log();
            log.setAppName(getNameFromPackageName(context, packageName));
            log.setAppPackage(packageName);
            log.setCamera(camera);
            log.setMic(mic);
            log.setTimestamp(convertSecondsToHMmSs(System.currentTimeMillis()));
            logList.add(log);
            String newCompleteLog = gson.toJson(logList);
            preferences.edit().putString("LOG.USAGE", newCompleteLog).apply();
        });
    }

    public static String convertSecondsToHMmSs(long millis) {
        Date date = new Date(millis);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(cal.getTimeZone());
        return formatter.format(date);
    }
}
