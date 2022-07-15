package com.aravi.dot.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "access_logs")
data class Log(
    @PrimaryKey
    var timestamp: Long,

    var packageName: String,
    var permission: String,

    var state: Int,
    var date: String,
)
