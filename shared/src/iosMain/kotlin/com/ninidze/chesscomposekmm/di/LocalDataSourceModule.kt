package com.ninidze.chesscomposekmm.di

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

@ExperimentalSettingsApi
actual val localDataStoreModule = module {
    single<FlowSettings> {
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults).toFlowSettings()
    }
}