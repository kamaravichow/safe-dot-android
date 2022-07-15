package com.aravi.dot.di

import com.aravi.dot.activities.log.LogsViewModel
import com.aravi.dot.database.AppDatabase
import com.aravi.dot.manager.DevLogger
import com.aravi.dot.manager.PreferenceManager
import com.aravi.dot.util.PermissionUtils
import com.aravi.dot.util.PiracyChecker
import com.aravi.dot.util.Utils
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    factory { Utils(androidContext()) }
    single { AppDatabase.database(androidContext()) }
    single { PreferenceManager(androidContext()) }
    single { DevLogger(get()) }
    single { PermissionUtils(androidContext(), get()) }
    single { PiracyChecker(androidContext(), get()) }

    viewModel { LogsViewModel(get()) }
}
