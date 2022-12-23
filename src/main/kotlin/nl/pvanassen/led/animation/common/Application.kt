package nl.pvanassen.led.animation.common

import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    ApplicationMain.main(args)
}

object ApplicationMain {
    fun main(args: Array<String>) {
        runBlocking {
            Context.controllerClient.start()
        }
    }
}
