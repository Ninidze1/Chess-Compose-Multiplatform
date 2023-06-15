package com.ninidze.chesscomposekmm.platform

expect class PlatformUtils() {
    fun randomFloat(): Float
    fun randomInt(bound: Int): Int
    fun currentTimeMillis(): Long
    fun arrayFill(array: ShortArray, value: Short)
    fun arrayFill(array: IntArray, value: Int)
    fun arrayFill(array: LongArray, value: Long)
}
