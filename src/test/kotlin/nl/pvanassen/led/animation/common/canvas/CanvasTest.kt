package nl.pvanassen.led.animation.common.canvas

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO


internal class CanvasTest {
    private val matrix = (0 until 16).map { 60 }
    private val canvas = Canvas(ImageIO.read(Canvas::class.java.getResourceAsStream("/mask-16-60.png")), matrix)

    @Test
    fun testCanvasWhiteBackBlackPixels() {
        val mask: BufferedImage = ImageIO.read(Canvas::class.java.getResourceAsStream("/test-mask1-16-60.png"))
        canvas.drawImage(mask)
        val lightStrips = canvas.getValues()
        assertThat(lightStrips).isNotEmpty
        lightStrips.forEach { assertThat(it).isEqualTo(0.toByte()) }
    }

    @Test
    fun testCanvasWhiteBackRedixels() {
        val mask: BufferedImage = ImageIO.read(Canvas::class.java.getResourceAsStream("/test-mask2-16-60.png"))
        canvas.drawImage(mask)
        val lightStrips = canvas.getValues()
        (lightStrips.indices step 3).forEach {
            val red = lightStrips[it].toInt() and 0xFF
            val green = lightStrips[it + 1].toInt() and 0xFF
            val blue = lightStrips[it + 2].toInt() and 0xFF
            assertThat(Color(red, green, blue).red).isEqualTo(255).overridingErrorMessage("Pos: $it")
        }
    }
}