package com.aravi.dot;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.aravi.dot.model.Log;
import com.aravi.dot.service.DotService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.aravi.dot.main.MainActivity.accessibilityPermission;

public class Utils {


    public static Dialog permissionsDialog;

    public static void showPermissionsDialog(Context context) {
        permissionsDialog = new Dialog(context, android.R.style.Theme_Material_Light_NoActionBar_TranslucentDecor);
        permissionsDialog.setContentView(R.layout.dialog_permissions);
        permissionsDialog.setCancelable(false);
        permissionsDialog.show();
        permissionsDialog.findViewById(R.id.permissions).setOnClickListener(view -> {
            if (!accessibilityPermission(context, DotService.class)) {
                context.startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS"));
            }
        });
    }

    public static void dismissPermissionDialog() {
        if (permissionsDialog != null && permissionsDialog.isShowing()) {
            permissionsDialog.setCancelable(true);
            permissionsDialog.dismiss();
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

        android.util.Log.i("LOG.TEST", "mic: " + mic + " camera :" + camera);
    }

    public static String convertSecondsToHMmSs(long millis) {
        Date date = new Date(millis);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(cal.getTimeZone());
        return formatter.format(date);
    }
}
