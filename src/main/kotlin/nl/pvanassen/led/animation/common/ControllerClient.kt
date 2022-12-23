package nl.pvanassen.led.animation.common

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.reduce
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.pvanassen.led.animation.common.canvas.Canvas
import nl.pvanassen.led.animation.common.model.*
import org.slf4j.LoggerFactory

class ControllerClient(private val controllerHost: String,
                       private val controllerPort: Int,
                       private val animationFactory: AnimationFactory<*>) {

    private val json = kotlinx.serialization.json.Json

    private val log = LoggerFactory.getLogger(this.javaClass)

    private val client = HttpClient {
        install(WebSockets) {
            maxFrameSize = Long.MAX_VALUE
            contentConverter = KotlinxWebsocketSerializationConverter(json)
        }
    }

    private var endpoint: AnimationEndpoint<*>? = null

    suspend fun start() {
        val session = client.webSocketSession(method = HttpMethod.Get, host = controllerHost, port = controllerPort, path = "/animation")
        while (true) {
            for (frame in session.incoming) {
                when (frame.frameType) {
                    FrameType.TEXT -> handleCommand(frame as Frame.Text, session)
                    FrameType.CLOSE -> handleFrameClose()
                    FrameType.BINARY, FrameType.PING, FrameType.PONG -> continue
                }
            }
        }
    }

    private fun handleFrameClose() {
        log.warn("Connection closed, now what?")
    }

    private suspend fun handleCommand(text: Frame.Text, session: DefaultClientWebSocketSession) {
        val message = json.parseToJsonElement(text.readText())
        val type = message.jsonObject["type"]!!.jsonPrimitive.content
        log.info("Received message of type {}", type)
        if (type == "welcome") {
            handleWelcome(json.decodeFromString(text.readText()))
            session.sendSerialized(Message("registration", animationFactory.getRegistrationInfo()))
        } else if (type == "request-animation") {
            val requestAnimation = json.decodeFromString<Message<RequestAnimation>>(text.readText())
            endpoint?.let {
                val frames = it.animate(requestAnimation.payload.fps, requestAnimation.payload.seconds)
                        .reduce { acc, value -> acc + value }
                session.send(frames)
            }
        }
    }

    private suspend fun handleWelcome(message: Message<StartClient>) {
        val pixels = message.payload.pixels
        val canvas = Canvas(MaskClient().fetchMask("http://${controllerHost}:${controllerPort}${message.payload.maskPath}"), pixels)
        val animation = animationFactory.getAnimation(canvas, pixels, Context.config)
        endpoint = AnimationEndpoint(animation)
    }
}