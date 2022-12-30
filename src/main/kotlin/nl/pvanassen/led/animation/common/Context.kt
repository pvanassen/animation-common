package nl.pvanassen.led.animation.common

import io.ktor.server.config.yaml.*
import nl.pvanassen.led.animation.common.model.AnimationFactory

object Context {
    val config = YamlConfig(null)!!

    private val controllerHost = config.property("app.controller.host").getString()
    private val controllerPort = config.property("app.controller.port").getString().toInt()
    private val animationFactoryClass = Class.forName(config.property("app.animation-factory").getString())
    private val animationFactory = animationFactoryClass.getDeclaredConstructor().newInstance() as AnimationFactory<*>

    val controllerClient = ControllerClient(controllerHost, controllerPort, animationFactory, config)

}