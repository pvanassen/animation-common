package nl.pvanassen.led.animation.common.util

import java.awt.Color

object ColorUtils {

    /**
     * Package red/green/blue values into a single integer.
     */
    fun makeColor(red: Int, green: Int, blue: Int): Int {
        val r = red and 0x000000FF
        val g = green and 0x000000FF
        val b = blue and 0x000000FF
        return r shl 16 or (g shl 8) or b
    }

    fun makeColorHSB(hue: Float, saturation: Float, brightness: Float): Int {
        return Color.HSBtoRGB(hue, saturation, brightness)
    }

    /**
     * Extract the red component from a color integer.
     */
    fun getRed(color: Int): Int {
        return color shr 16 and 0x000000FF
    }

    /**
     * Extract the green component from a color integer.
     */
    fun getGreen(color: Int): Int {
        return color shr 8 and 0x000000FF
    }

    /**
     * Extract the blue component from a color integer.
     */
    fun getBlue(color: Int): Int {
        return color and 0x000000FF
    }

    /**
     * Return a color that has been faded by the given brightness.
     *
     * @param brightness a number from 0 to 255.
     * @return a new color.
     */
    fun fadeColor(c: Int, brightness: Int): Int {
        var r = c shr 16 and 0x000000FF
        var g = c shr 8 and 0x000000FF
        var b = c and 0x000000FF
        val newbr = brightness / 255f
        r = (r * newbr).toInt()
        g = (g * newbr).toInt()
        b = (b * newbr).toInt()
        return r shl 16 or (g shl 8) or b
    }

    fun mixColor(c1: Int, c2: Int): Int {
        val red = (getRed(c1) + getRed(c2)) / 2f
        val green = (getGreen(c1) + getGreen(c2)) / 2f
        val blue = (getBlue(c1) + getBlue(c2)) / 2f
        return makeColor(red.toInt(), green.toInt(), blue.toInt())
    }
}
