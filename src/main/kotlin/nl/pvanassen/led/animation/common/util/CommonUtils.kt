package nl.pvanassen.led.animation.common.util

import java.util.*

object CommonUtils {
    private val random = Random()

    fun getRandom(max: Int): Int {
        return getRandom(0, max)
    }

    fun getRandom(start: Int, end: Int): Int {
        return random.nextInt(end - start) + start
    }
}