package com.aravi.dot.extensions

import android.app.Activity
import androidx.core.content.ContextCompat


fun Activity.permission(permission: String) {
    if (ContextCompat.checkSelfPermission(this, permission) != 0) {
        requestPermissions(arrayOf(permission), 12030)
    }
}
