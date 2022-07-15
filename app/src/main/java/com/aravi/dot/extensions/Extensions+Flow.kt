package com.aravi.dot.extensions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

suspend fun IntentFilter.asFlow(context: Context) = callbackFlow<Intent?> {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            trySend(intent).isSuccess
        }
    }
    context.registerReceiver(receiver, this@asFlow)
    awaitClose {
        context.unregisterReceiver(receiver)
    }
}
