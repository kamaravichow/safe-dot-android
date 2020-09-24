package com.aravi.dot;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.aravi.dot.service.DotService;

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
}
