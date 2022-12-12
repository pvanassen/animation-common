package nl.pvanassen.led.animation.common.canvas

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.math.max

/**
 * Canvas class to draw pixels with
 */
public class Canvas(private val strips: Int, private val ledsPerStrip: Int) {

    private val mask: BufferedImage =
        ImageIO.read(Canvas::class.java.getResourceAsStream("/mask-$strips-$ledsPerStrip.png"))
    val canvas: BufferedImage
    private val positions: Positions

    init {
        canvas = BufferedImage(mask.width, mask.height, BufferedImage.TYPE_INT_RGB)
        // getRGB returns int in TYPE_INT_ARGB
        val positionsList = (0 until mask.width)
            .flatMap { x ->
                (0 until mask.height)
                    .map { y -> Triple(x, y, mask.getRGB(x, y) and 0xFFFFFF) }
            }
            .filter { it.third != 0 }
            .map { Position(it.third shr 16 and 0xFF, it.third and 0xFF, it.first, it.second) }
        positions = Positions(positionsList, strips, ledsPerStrip)
    }

    fun getValues(): ByteArray {
        val values = ByteArray((strips * ledsPerStrip * 3))
        for (strip in 0 until strips) {
            for (pixel in 0 until ledsPerStrip) {
                val x = positions.getX(strip, pixel)
                val y = positions.getY(strip, pixel)
                val color = canvas.getRGB(x, y)
                val red: Byte = (color shr 16 and 0xFF).toByte()
                val green: Byte = (color shr 8 and 0xFF).toByte()
                val blue: Byte = (color and 0xFF).toByte()
                val base = (strip * ledsPerStrip * 3) + pixel * 3
                values[base] = red
                values[base + 1] = green
                values[base + 2] = blue
            }
        }
        return values
    }

    fun setValue(strip: Int, pixel: Int, color: Int) {
        val x = positions.getX(strip, pixel)
        val y = positions.getY(strip, pixel)
        canvas.setRGB(x, y, color)
    }

    fun setImage(offsetX: Int, offsetY: Int, image: BufferedImage, outOfBoundsBlack: Boolean = true) {
        for (strip in 0 until strips) {
            for (pixel in 0 until ledsPerStrip) {
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

    private class Positions(private val positions: List<Position>, strips: Int, pixels: Int) {

        private val raster: Array<Array<Position>>

        init {
            raster = (0..strips).map { strip ->
                positions
                    .filter { it -> it.strip == strip }
            }
                .map { stripPositions ->
                    stripPositions
                        .filter { it.pixel in (0..pixels) }
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