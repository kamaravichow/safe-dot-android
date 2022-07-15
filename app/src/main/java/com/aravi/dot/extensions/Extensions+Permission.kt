package com.aravi.dot.extensions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun Context.doesHavePermissions(vararg permissions: String): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}
