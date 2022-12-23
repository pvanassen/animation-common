package nl.pvanassen.led.animation.common.model

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class MaskClient {
    suspend fun fetchMask(maskUrl: String): BufferedImage {
        return HttpClient(CIO).use {
            return@use ImageIO.read(it.get(maskUrl).bodyAsChannel().toInputStream())
        }
    }
}