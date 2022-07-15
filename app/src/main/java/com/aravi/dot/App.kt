/*
 * Copyright (C) 2021.  Aravind Chowdary (@kamaravichow)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.aravi.dot

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.aravi.dot.di.appModule
import me.aravi.commons.base.BaseApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        localNotificationSetup(this)

        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

    }

    private fun localNotificationSetup(application: Application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.SERVICE_NOTIFICATION_CHANNEL,
                "Service Notification",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description =
                "This keeps the app alive. We recommend not to disable this notification."
            channel.enableLights(false)
            channel.setShowBadge(true)
            channel.enableVibration(false)
            val manager = application.getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.DEFAULT_NOTIFICATION_CHANNEL,
                "Default Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "This shows what app is using the camera or mic or location"
            channel.enableLights(true)
            channel.setShowBadge(true)
            channel.enableVibration(false)
            val manager = application.getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }
}
