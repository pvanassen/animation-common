package nl.pvanassen.led.animation.common.model

import kotlinx.serialization.Serializable

@Serializable
data class StartClient(val maskPath: String, val pixels: List<Int>)
