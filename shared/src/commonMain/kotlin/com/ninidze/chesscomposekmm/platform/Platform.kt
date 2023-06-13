package com.ninidze.chesscomposekmm.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform