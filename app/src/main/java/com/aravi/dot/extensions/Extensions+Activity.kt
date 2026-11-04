package com.aravi.dot.extensions

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


fun Activity.permission(permission: String) {
    if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), 12030)
    }
}

fun Activity.permissions(vararg permissions: String) {
    ActivityCompat.requestPermissions(this, arrayOf(*permissions), 12030)
}
