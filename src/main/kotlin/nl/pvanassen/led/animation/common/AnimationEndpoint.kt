package nl.pvanassen.led.animation.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import nl.pvanassen.led.animation.common.model.Animation
import org.slf4j.LoggerFactory
import java.util.*

class AnimationEndpoint<T>(val animation: Animation<T>) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val random = Random()

    fun animate(seconds: Int, fps: Int): Flow<ByteArray> {
        logger.info("Received request for $seconds seconds with $fps fps")

        if (seconds == -1 && !animation.isFixedTimeAnimation()) {
            throw IllegalArgumentException("One must request a positive amount of seconds")
        }

        val secondsPerFrame = 1 / fps.toDouble()
        val msPerFrame = secondsPerFrame * 1_000
        val nsPerFrame = msPerFrame * 1_000_000
        val seed = random.nextLong()
        return if (seconds > 0) {
            getAnimation(seconds * fps, seed, nsPerFrame.toInt())
        } else {
            getFixedTimeAnimation(seed, nsPerFrame.toInt(), animation.getStateObject())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getAnimation(frames: Int, seed: Long, nsPerFrame: Int): Flow<ByteArray> {
        val state = animation.getStateObject()
        return flow { emit(frames) }
                .flatMapConcat { getFrames(seed, it, nsPerFrame, state) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getFixedTimeAnimation(seed: Long, nsPerFrame: Int, state: T): Flow<ByteArray> =
            flow { emit(animation.getFixedTimeAnimationFrames(state)) }
                    .flatMapConcat { getFrames(seed, it, nsPerFrame, state) }

    private fun getFrames(seed: Long, frames: Int, nsPerFrame: Int, helperObject: T): Flow<ByteArray> =
            flow {
                (0 until frames).forEach {
                    emit(it)
                }
            }
                    .map {
                        animation.getFrame(seed, it, nsPerFrame, helperObject)
                    }
}