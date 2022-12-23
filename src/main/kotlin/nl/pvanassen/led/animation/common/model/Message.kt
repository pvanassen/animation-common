package nl.pvanassen.led.animation.common.model

import kotlinx.serialization.Serializable

@Serializable
data class Message<T>(val type: String, val payload: T)