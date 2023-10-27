package nl.pvanassen.led.animation.common.canvas

import java.awt.Image
import java.awt.image.BufferedImage
import kotlin.math.max

/**
 * Canvas class to draw pixels with
 */
class Canvas(private val mask: BufferedImage,
             private val pixels: List<Int>) {

    private val canvas: BufferedImage = BufferedImage(mask.width, mask.height, BufferedImage.TYPE_INT_RGB)
    private val totalPixels = pixels.sum()
    private val positions: Positions

    init {
        // getRGB returns int in TYPE_INT_ARGB
        val positionsList = (0 until mask.width)
                .flatMap { x ->
                    (0 until mask.height)
                            .map { y -> Triple(x, y, mask.getRGB(x, y) and 0xFFFFFF) }
                }
                .filter { it.third != 0 }
                .map { Position(it.third shr 16 and 0xFF, it.third and 0xFF, it.first, it.second) }
        positions = Positions(positionsList, pixels)
    }

    fun getValues(): ByteArray {
        var base = 0
        val values = ByteArray((totalPixels * 3))
        for (strip in pixels.indices) {
            for (pixel in 0 until pixels[strip]) {
                val x = positions.getX(strip, pixel)
                val y = positions.getY(strip, pixel)
                val color = canvas.getRGB(x, y)
                val red: Byte = (color shr 16 and 0xFF).toByte()
                val green: Byte = (color shr 8 and 0xFF).toByte()
                val blue: Byte = (color and 0xFF).toByte()
                values[base] = red
                values[base + 1] = green
                values[base + 2] = blue
                base += 3
            }
        }
        return values
    }

    fun setValue(strip: Int, pixel: Int, color: Int) {
        val x = positions.getX(strip, pixel)
        val y = positions.getY(strip, pixel)
        canvas.setRGB(x, y, color)
    }

    fun setRGB(x: Int, y: Int, rgb: Int) {
        canvas.setRGB(x, y, rgb)
    }

    fun setImage(offsetX: Int, offsetY: Int, image: BufferedImage, outOfBoundsBlack: Boolean = true) {
        for (strip in pixels.indices) {
            for (pixel in 0 until pixels[strip]) {
                val x = positions.getX(strip, pixel) + offsetX
                val y = positions.getY(strip, pixel) + offsetY
                val color = if ((x >= image.width || y >= image.height) && outOfBoundsBlack) {
                    0
                } else {
                    image.getRGB(max(0, x), max(0, y))
                }
                canvas.setRGB(positions.getX(strip, pixel), positions.getY(strip, pixel), color)
            }
        }
    }

    fun drawImage(img: Image) {
        canvas.graphics.drawImage(img, 0, 0, null)
    }

    fun getWidth() = canvas.width

    fun getHeight() = canvas.height

    private class Positions(private val positions: List<Position>, pixelStrips: List<Int>) {

        private val raster: Array<Array<Position>>

        init {
            raster = (0..pixelStrips.size).map { strip ->
                Pair(strip, positions.filter { it.strip == strip })
            }
                    .map { stripPositions ->
                        stripPositions.second
                                .filter { it.pixel in (0..pixelStrips[stripPositions.first]) }
                                .sortedBy { it.pixel }
                                .toTypedArray()
                    }.toTypedArray()
        }

        fun getX(strip: Int, pixel: Int): Int {
            return raster[strip][pixel].x
        }

        fun getY(strip: Int, pixel: Int): Int {
            return raster[strip][pixel].y
        }

        override fun toString(): String {
            return raster.toString()
        }

    }

    private data class Position(val strip: Int, val pixel: Int, val x: Int, val y: Int)
}