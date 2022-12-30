package nl.pvanassen.led.animation.common.model

import kotlinx.serialization.Serializable

/**
 * Registration data
 *
 * @param name Name of the animation (if name is already present on remote, it will be replaced)
 * @param type What type of animation?
 * @param cron Cron on when to run, required (and only parsed) when type is TIMED.
 *               See <a href="https://github.com/Scogun/kcron-common">KCron</a> for syntax
 */
@Serializable
data class Registration(val name: String,
                        val type: AnimationType = AnimationType.NORMAL,
                        val cron: String = "")

enum class AnimationType {
    /**
     * Normal animation, runs in the loop
     */
    NORMAL,

    /**
     * Only run this animation on startup
     */
    ON_STARTUP,

    /**
     * Only run this animation on shutdown
     */
    ON_SHUTDOWN,

    /**
     * Only run at specific times. Requires a cron spec
     */
    TIMED,

    /**
     * A little gimmick
     */
    FIREWORKS
}
