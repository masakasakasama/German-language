package com.masakasakasama.reisedeutsch.ui

import android.animation.ValueAnimator
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.masakasakasama.reisedeutsch.R

@Composable fun AnimatedTravelCompanion() {
    ReiseCard(Modifier.fillMaxWidth()) { CompanionCharacter(Modifier.fillMaxWidth().height(290.dp), controls = true) }
}

/** One accepted character asset for every screen. Tap explicitly enables motion even if auto motion is off. */
@Composable fun CompanionCharacter(modifier: Modifier = Modifier, controls: Boolean = false) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var active by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    var systemMotion by remember { mutableStateOf(ValueAnimator.areAnimatorsEnabled()) }
    var paused by rememberSaveable { mutableStateOf(false) }
    var manualPlay by rememberSaveable { mutableStateOf(false) }
    var request by remember { mutableIntStateOf(0) }
    var consumedRequest by remember { mutableIntStateOf(0) }
    var pose by remember { mutableStateOf(CompanionPose(0f, 0f, 1f)) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                active = true
                systemMotion = ValueAnimator.areAnimatorsEnabled()
            }
            if (event == Lifecycle.Event.ON_PAUSE) active = false
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val running = companionRuns(active, paused, systemMotion, manualPlay)
    LaunchedEffect(running, request) {
        pose = CompanionPose(0f, 0f, 1f)
        if (running) {
            val react = request != consumedRequest
            consumedRequest = request
            val start = withFrameNanos { it }
            while (true) {
                withFrameNanos { now -> pose = companionPose((now - start) / 1_000_000_000f, react) }
            }
        }
    }
    val play: () -> Unit = { paused = false; manualPlay = true; request++ }
    Image(
        painterResource(R.drawable.travel_companion),
        contentDescription = "旅のうさぎ。タップして動かす",
        contentScale = ContentScale.Fit,
        modifier = modifier.padding(if (controls) 28.dp else 4.dp)
            .graphicsLayer {
                val amplitude = if (controls) 1f else .32f
                translationY = -pose.lift * amplitude * density
                rotationZ = pose.tilt
                scaleX = pose.scale
                scaleY = pose.scale
            }
            .clickable(enabled = active, role = Role.Button, onClickLabel = "うさぎを動かす", onClick = play)
    )
    if (controls) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = play, enabled = active) { Text("▶ うさぎを動かす") }
            TextButton(onClick = { if (running) paused = true else play() }, enabled = active) {
                Text(if (running) "動きを止める" else "動きを再開")
            }
        }
        if (!systemMotion && !manualPlay) Text("自動の動きはOFFです。「うさぎを動かす」で再生できます。")
    }
}
