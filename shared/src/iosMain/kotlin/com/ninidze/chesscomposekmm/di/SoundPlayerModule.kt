package com.ninidze.chesscomposekmm.di

import com.ninidze.chesscomposekmm.domain.sound.SoundPlayer
import com.ninidze.chesscomposekmm.platform.IosSoundPlayer
import org.koin.dsl.module

actual val soundPlayerModule = module {
    single<SoundPlayer> { IosSoundPlayer() }
}
