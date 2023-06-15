package com.ninidze.chesscomposekmm.platform

import java.util.Arrays
import java.util.Random

actual class PlatformUtils {
    private val random = Random()

    actual fun randomFloat(): Float = random.nextFloat()
    actual fun randomInt(bound: Int): Int = random.nextInt(bound)
    actual fun currentTimeMillis(): Long = System.currentTimeMillis()
    actual fun arrayFill(array: ShortArray, value: Short) { Arrays.fill(array, value) }
    actual fun arrayFill(array: IntArray, value: Int) { Arrays.fill(array, value) }
    actual fun arrayFill(array: LongArray, value: Long) { Arrays.fill(array, value) }

}
