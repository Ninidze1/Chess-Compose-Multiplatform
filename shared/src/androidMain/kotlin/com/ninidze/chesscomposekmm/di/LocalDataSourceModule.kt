package com.ninidze.chesscomposekmm.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val Context.myDataStore by preferencesDataStore(name = "settings")

actual val localDataStoreModule = module {
    single {
        val context = androidContext()
        context.myDataStore
    }

    single<FlowSettings> {
        val datastore = get<DataStore<Preferences>>()
        DataStoreSettings(datastore = datastore)
    }
}
