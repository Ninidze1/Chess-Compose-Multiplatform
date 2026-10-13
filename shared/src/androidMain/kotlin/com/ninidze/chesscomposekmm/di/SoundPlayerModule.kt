package com.ninidze.chesscomposekmm.di

import com.ninidze.chesscomposekmm.domain.sound.SoundPlayer
import com.ninidze.chesscomposekmm.platform.AndroidSoundPlayer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val soundPlayerModule = module {
    single<SoundPlayer> { AndroidSoundPlayer(androidContext()) }
}
