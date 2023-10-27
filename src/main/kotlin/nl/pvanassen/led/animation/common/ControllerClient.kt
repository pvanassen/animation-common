package nl.pvanassen.led.animation.common

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.config.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.pvanassen.led.animation.common.canvas.Canvas
import nl.pvanassen.led.animation.common.model.*
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

class ControllerClient(private val controllerHost: String,
                       private val controllerPort: Int,
                       private val animationFactory: AnimationFactory<*>,
                       private val config: ApplicationConfig) {

    private val json = kotlinx.serialization.json.Json

    private val log = LoggerFactory.getLogger(this.javaClass)

    private val client = HttpClient {
        install(WebSockets) {
            maxFrameSize = Long.MAX_VALUE
            pingInterval = 500L
            contentConverter = KotlinxWebsocketSerializationConverter(json)
        }
    }

    private var endpoint: AnimationEndpoint<*>? = null

    suspend fun start() {
        while (true) {
            val session = try {
                client.webSocketSession(
                        method = HttpMethod.Get,
                        host = controllerHost,
                        port = controllerPort,
                        path = "/animation"
                )
            } catch (e: Exception) {
                log.error("Error in setting up connection", e)
                null
            }
            session?.let {
                while (it.isActive) {
                    for (frame in it.incoming) {
                        when (frame.frameType) {
                            FrameType.TEXT -> handleCommand(frame as Frame.Text, it)
                            FrameType.CLOSE -> handleFrameClose()
                            FrameType.PING, FrameType.PONG -> log.info("Ping? Pong? ${frame.frameType}")
                            FrameType.BINARY -> continue
                        }
                    }
                }
            }
            log.info("Connection died. Retrying in 15 seconds")
            delay(15.seconds)
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
            session.sendSerialized(Message("registration", animationFactory.getRegistrationInfo(config)))
        } else if (type == "request-animation") {
            val requestAnimation = json.decodeFromString<Message<RequestAnimation>>(text.readText())
            endpoint?.let {
                try {
                    val frames = it.animate(requestAnimation.payload.seconds, requestAnimation.payload.fps)
                            .reduce { acc, value -> acc + value }
                    session.send(frames)
                } catch (e: Exception) {
                    log.warn("Error sending frame", e)
                    throw e
                }
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