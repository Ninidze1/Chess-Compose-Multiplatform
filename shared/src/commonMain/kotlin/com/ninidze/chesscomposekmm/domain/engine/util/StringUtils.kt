package com.ninidze.chesscomposekmm.domain.engine.util

object StringUtils {
    private const val SPACES = "                     "

    fun padLeft(str: String, totalChars: Int): String {
        return SPACES.substring(0, totalChars - str.length) + str
    }
}
