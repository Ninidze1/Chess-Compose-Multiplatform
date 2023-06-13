package com.ninidze.chesscomposekmm

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform