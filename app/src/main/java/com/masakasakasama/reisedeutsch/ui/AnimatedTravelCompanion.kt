package com.masakasakasama.reisedeutsch.ui

import android.animation.ValueAnimator
import android.graphics.BitmapFactory
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.masakasakasama.reisedeutsch.R

@Composable fun AnimatedTravelCompanion() {
    ReiseCard(Modifier.fillMaxWidth()) { CompanionCharacter(Modifier.fillMaxWidth().height(290.dp), controls = true) }
}

/** Connected 2D puppet mesh of the accepted art; local joints, not whole-image transforms. */
@Composable fun CompanionCharacter(modifier: Modifier = Modifier, controls: Boolean = false) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var active by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    var systemMotion by remember { mutableStateOf(ValueAnimator.areAnimatorsEnabled()) }
    var paused by rememberSaveable { mutableStateOf(false) }
    var manualPlay by rememberSaveable { mutableStateOf(false) }
    var request by remember { mutableIntStateOf(0) }
    var action by remember { mutableStateOf(CompanionAction.IDLE) }
    var consumedRequest by remember { mutableIntStateOf(0) }
    var pose by remember { mutableStateOf(CompanionPose()) }
    var sceneSeconds by remember { mutableFloatStateOf(0f) }
    val context = LocalContext.current
    val bitmap = remember(context) { BitmapFactory.decodeResource(context.resources, R.drawable.travel_companion) }
    val paint = remember { Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG) }
    val vertices = remember { FloatArray(49 * 49 * 2) }
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
        if (running) {
            val react = request != consumedRequest
            consumedRequest = request
            val baseTime = sceneSeconds
            val start = withFrameNanos { it }
            while (true) {
                withFrameNanos { now ->
                    val elapsed = (now - start) / 1_000_000_000f
                    sceneSeconds = baseTime + elapsed
                    pose = companionPose(sceneSeconds,
                        if (react && action == CompanionAction.WAVE && elapsed <= 2.4f) elapsed else -1f,
                        if (react) action else CompanionAction.IDLE, elapsed)
                }
            }
        }
    }
    val perform: (CompanionAction) -> Unit = { next -> action = next; paused = false; manualPlay = true; request++ }
    val play: () -> Unit = { perform(CompanionAction.WAVE) }
    Canvas(modifier = modifier.padding(if (controls) 18.dp else 3.dp)
        .semantics { contentDescription = "旅のうさぎ。タップで手を振ります" }
        .clickable(enabled = active, role = Role.Button, onClickLabel = "うさぎが手を振る", onClick = play)) {
        val scale = minOf(size.width / bitmap.width, size.height / bitmap.height) * .88f
        val width = bitmap.width * scale
        val height = bitmap.height * scale
        val left = (size.width - width) / 2f
        val top = size.height - height
        val mesh = deformCompanionMesh(pose)
        for (i in mesh.indices step 2) {
            vertices[i] = left + mesh[i] * width
            vertices[i+1] = top + mesh[i+1] * height
        }
        drawIntoCanvas { it.nativeCanvas.drawBitmapMesh(bitmap, 48, 48, vertices, 0, null, 0, paint) }
    }
    if (controls) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = play, enabled = active) { Text("手を振る") }
            TextButton(onClick = { perform(CompanionAction.WALK) }, enabled = active) { Text("足踏み") }
            TextButton(onClick = { perform(CompanionAction.JUMP) }, enabled = active) { Text("ジャンプ") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = { if (running) paused = true else play() }, enabled = active) {
                Text(if (running) "動きを止める" else "動きを再開")
            }
        }
        if (!systemMotion && !manualPlay) Text("自動の動きはOFFです。「手を振る」で再生できます。")
    }
}
