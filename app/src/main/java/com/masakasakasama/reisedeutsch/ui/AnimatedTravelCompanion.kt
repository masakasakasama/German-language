package com.masakasakasama.reisedeutsch.ui

import android.animation.ValueAnimator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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

/** Animate the accepted artwork as one sprite so shoulders/hands/luggage stay connected. */
@Composable fun AnimatedTravelCompanion() {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var resumed by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    var systemMotion by remember { mutableStateOf(ValueAnimator.areAnimatorsEnabled()) }
    var paused by rememberSaveable { mutableStateOf(false) }
    var reactionRequest by remember { mutableIntStateOf(0) }
    val reaction = remember { Animatable(0f) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                resumed = true
                systemMotion = ValueAnimator.areAnimatorsEnabled()
            }
            if (event == Lifecycle.Event.ON_PAUSE) resumed = false
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val moving = resumed && !paused && systemMotion
    LaunchedEffect(reactionRequest, moving) {
        reaction.snapTo(0f)
        if (reactionRequest > 0 && moving) {
            reaction.animateTo(0f, keyframes {
                durationMillis = 900
                0f at 0 using FastOutSlowInEasing
                1f at 230 using FastOutSlowInEasing
                0f at 480 using FastOutSlowInEasing
                .35f at 650 using FastOutSlowInEasing
                0f at 900
            })
        }
    }
    // Removing the transition from composition stops frames while paused or backgrounded.
    val floatAmount = if (moving) {
        val transition = rememberInfiniteTransition(label = "companion idle")
        val amount by transition.animateFloat(
            initialValue = -1f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "gentle floating"
        )
        amount
    } else 0f
    ReiseCard(Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(R.drawable.travel_companion),
            contentDescription = "赤い帽子の旅のうさぎ。タップすると弾みます",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().height(290.dp).padding(18.dp)
                .graphicsLayer {
                    translationY = (-3f * floatAmount - 15f * reaction.value) * density
                    rotationZ = .8f * floatAmount + 2f * reaction.value
                    scaleX = 1f + .018f * reaction.value
                    scaleY = scaleX
                }
                .clickable(enabled = moving, role = Role.Button, onClickLabel = "うさぎと出発") { reactionRequest++ }
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(enabled = moving, onClick = { reactionRequest++ }) { Text("いっしょに出発！") }
            TextButton(enabled = systemMotion, onClick = { paused = !paused }) {
                Text(if (!systemMotion) "動きを抑える設定中" else if (paused) "動きを再開" else "動きを止める")
            }
        }
    }
}
