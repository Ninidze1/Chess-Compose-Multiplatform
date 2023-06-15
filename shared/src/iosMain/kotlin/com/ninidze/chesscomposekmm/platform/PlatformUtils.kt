package com.ninidze.chesscomposekmm.platform

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import kotlin.random.Random

actual class PlatformUtils{
    private val random = Random.Default

    actual fun randomFloat(): Float = random.nextFloat()
    actual fun randomInt(bound: Int): Int = random.nextInt(bound)
    actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

    actual fun arrayFill(array: ShortArray, value: Short) {
        for (i in array.indices) array[i] = value
    }
    actual fun arrayFill(array: IntArray, value: Int) {
        for (i in array.indices) array[i] = value
    }
    actual fun arrayFill(array: LongArray, value: Long) {
        for (i in array.indices) array[i] = value
    }

}
