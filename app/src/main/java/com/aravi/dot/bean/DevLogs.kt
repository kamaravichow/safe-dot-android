package com.aravi.dot.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dev_logs")
data class DevLogs(
    @PrimaryKey
    val timestamp: Long,

    val message: String,
    val type: String = "debug",
)
