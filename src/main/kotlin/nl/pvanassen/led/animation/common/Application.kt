package nl.pvanassen.led.animation.common

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.config.yaml.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import nl.pvanassen.led.animation.common.canvas.Canvas
import nl.pvanassen.led.animation.common.model.AnimationFactory
import nl.pvanassen.led.animation.common.model.LedPanelModel

fun main(args: Array<String>) {
    embeddedServer(CIO, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    routing {
        val config = YamlConfig(null)!!
        val strips = config.property("app.strips").getString().toInt()
        val ledsPerStrip = config.property("app.leds-per-strip").getString().toInt()
        val canvas = Canvas(strips, ledsPerStrip)
        val ledPanelModel = LedPanelModel(strips, ledsPerStrip)
        val animationFactoryClass = Class.forName(config.property("app.animation-factory").getString())
        val animationFactory = animationFactoryClass.getDeclaredConstructor().newInstance() as AnimationFactory<*>
        val animation = animationFactory.getAnimation(canvas, ledPanelModel, config)
        val endpoint = AnimationEndpoint(animation)

        get("/animation/{seconds}/{fps}") {
            val seconds = call.parameters["seconds"]!!.toInt()
            val fps = call.parameters["fps"]!!.toInt()
            call.respondBytesWriter(contentType = ContentType.Application.OctetStream) {
                val job = launch {
                    endpoint.animate(seconds, fps).collect {
                        try {
                            it.forEach { b -> writeByte(b) }
                            flush()
                        } catch (_: ChannelWriteException) {
                            cancel()
                        }
                    }
                }
                job.join()
            }
        }
    }
}
