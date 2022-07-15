package com.aravi.dot.util

import android.content.Context
import com.aravi.dot.manager.PreferenceManager
import com.aravi.dot.R
import com.github.javiersantos.piracychecker.PiracyChecker
import com.github.javiersantos.piracychecker.callbacks.PiracyCheckerCallback
import com.github.javiersantos.piracychecker.enums.PiracyCheckerError
import com.github.javiersantos.piracychecker.enums.PirateApp

class PiracyChecker(val context: Context, val preferenceManager: PreferenceManager) {

    private fun init() {
        PiracyChecker(context)
            .enableGooglePlayLicensing(context.getString(R.string.license_key).trim { it <= ' ' })
            .callback(object : PiracyCheckerCallback() {
                override fun allow() {
                    preferenceManager.integrity = true
                }

                override fun doNotAllow(error: PiracyCheckerError, app: PirateApp?) {
                    preferenceManager.integrity = false
                }
            })
            .start()
    }

    private fun isGood(): Boolean {
        return preferenceManager.integrity && preferenceManager.dotIntegrity
    }
}
