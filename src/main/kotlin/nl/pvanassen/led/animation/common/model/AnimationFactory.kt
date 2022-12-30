package nl.pvanassen.led.animation.common.model

import io.ktor.server.config.*
import nl.pvanassen.led.animation.common.canvas.Canvas

interface AnimationFactory<T> {
    fun getAnimation(canvas: Canvas,
                     pixels: List<Int>,
                     config: ApplicationConfig
    ): Animation<T>

    fun getRegistrationInfo(config: ApplicationConfig): Registration
}