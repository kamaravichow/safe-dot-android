package com.aravi.dot.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_analysis")
data class AppBean(

    @PrimaryKey
    val packageName: String,

    val hasCameraPermission: Boolean,
    val hasMicPermission: Boolean,
    val hasLocationPermission: Boolean,
)
