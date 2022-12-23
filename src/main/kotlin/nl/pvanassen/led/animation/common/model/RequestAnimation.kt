package nl.pvanassen.led.animation.common.model

import kotlinx.serialization.Serializable

@Serializable
data class RequestAnimation(val seconds: Int, val fps: Int)
