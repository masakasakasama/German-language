package com.masakasakasama.reisedeutsch.ui

import kotlin.math.PI
import kotlin.math.sin

data class CompanionPose(val lift: Float, val tilt: Float, val scale: Float)
fun companionRuns(active: Boolean, paused: Boolean, systemMotion: Boolean, manualPlay: Boolean) =
    active && !paused && (systemMotion || manualPlay)

/** Frame-clock motion, independent of Android's animator duration scale after explicit play. */
fun companionPose(seconds: Float, reacting: Boolean): CompanionPose {
    val phase = seconds * (2f * PI.toFloat() / 2.4f)
    val bounce = if (reacting && seconds < 1.2f) {
        kotlin.math.abs(sin(seconds * PI.toFloat() / .6f)) * (1f - seconds / 1.2f)
    } else 0f
    return CompanionPose(8f * sin(phase) + 24f * bounce, 3f * sin(phase), 1f + .045f * bounce)
}
