package nl.pvanassen.led.animation.common.model

interface Animation<T> {

    fun getFrame(seed: Long, frame: Int, nsPerFrame: Int, helper: T): ByteArray

    fun isFixedTimeAnimation(): Boolean = false

    fun getFixedTimeAnimationFrames(helper: T): Int = 0

    @Suppress("UNCHECKED_CAST")
    fun getStateObject(): T {
        return Object() as T
    }
}