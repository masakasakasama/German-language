package com.masakasakasama.reisedeutsch.ui

import kotlin.math.*

enum class CompanionAction { IDLE, WAVE, WALK, JUMP }

data class CompanionPose(val breath: Float = 0f, val head: Float = 0f, val ear: Float = 0f,
                         val shoulder: Float = 0f, val elbow: Float = 0f, val wrist: Float = 0f,
                         val leftStep: Float = 0f, val rightStep: Float = 0f,
                         val crouch: Float = 0f, val jumpHeight: Float = 0f, val bodyBob: Float = 0f)
fun companionRuns(active: Boolean, paused: Boolean, systemMotion: Boolean, manualPlay: Boolean) =
    active && !paused && (systemMotion || manualPlay)

/** Continuous breathing plus a softly eased two-second greeting, never whole-image rotation. */
fun companionPose(seconds: Float, waveAge: Float = -1f, action: CompanionAction = CompanionAction.IDLE, actionAge: Float = 0f): CompanionPose {
    val phase = seconds * (2f * PI.toFloat() / 3.6f)
    val autoAge = seconds % 9f - 2f
    val age = if (waveAge >= 0f) waveAge else autoAge
    val wave = if (age in 0f..2.4f) {
        val envelope = sin(PI.toFloat() * age / 2.4f).pow(2)
        envelope * sin(age * 2f * PI.toFloat() * 1.65f)
    } else 0f
    val walking = action == CompanionAction.WALK && actionAge in 0f..4f
    val stride = if (walking) sin(actionAge * 2f * PI.toFloat()) * sin(PI.toFloat()*actionAge/4f).pow(2) else 0f
    val jumping = action == CompanionAction.JUMP && actionAge in 0f..1.35f
    val crouch = when {
        jumping && actionAge in 0f.. .35f -> sin(PI.toFloat()*actionAge/.35f).pow(2)
        jumping && actionAge in .95f..1.35f -> .7f*sin(PI.toFloat()*(actionAge-.95f)/.4f).pow(2)
        else -> 0f
    }
    val flight = if (jumping && actionAge in .35f.. .95f) (actionAge-.35f)/.6f else 0f
    val jump = .10f*4f*flight*(1f-flight)
    val activeWave = if (walking || jumping) 0f else wave
    return CompanionPose(sin(phase), .012f * sin(phase * .5f),
        .024f * (sin(phase * .5f - .45f) + sin(.45f)) + .055f*crouch - .18f*jump,
        .035f * activeWave - .025f*stride, .10f * activeWave, .20f * activeWave,
        stride, -stride, crouch + jump*2f, jump, if (walking) -.006f*abs(stride) else 0f)
}

private const val ART_ASPECT = 1374f / 1145f
private fun smoothWeight(distance: Float): Float {
    val t = (1f - distance).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}
private fun oval(x: Float, y: Float, cx: Float, cy: Float, rx: Float, ry: Float): Float =
    smoothWeight(sqrt(((x-cx)/rx).pow(2) + ((y-cy)/ry).pow(2)))
private fun capsule(x:Float,y:Float,ax:Float,ay:Float,bx:Float,by:Float,r:Float):Float {
    val dx=bx-ax; val dy=by-ay
    val t=(((x-ax)*dx+(y-ay)*dy)/(dx*dx+dy*dy)).coerceIn(0f,1f)
    return smoothWeight(hypot((x-ax-t*dx)*ART_ASPECT,y-ay-t*dy)/r)
}

/** One shared connected mesh. Smooth influence fields prevent separated shoulders/cuffs. */
fun deformCompanionMesh(p: CompanionPose, columns: Int = 48, rows: Int = 48): FloatArray {
    require(columns > 0 && rows > 0)
    val result = FloatArray((columns+1)*(rows+1)*2)
    var index=0
    for (row in 0..rows) for (column in 0..columns) {
        val x=column.toFloat()/columns; val y=row.toFloat()/rows
        var px=x; var py=y
        fun rotate(cx:Float,cy:Float,angle:Float,weight:Float) {
            val a=angle*weight; val dx=(px-cx)*ART_ASPECT; val dy=py-cy
            px=cx+(dx*cos(a)-dy*sin(a))/ART_ASPECT
            py=cy+dx*sin(a)+dy*cos(a)
        }
        // Chest expands subtly while planted feet and luggage stay fixed.
        val chest=oval(x,y,.475f,.61f,.22f,.23f)
        px+=(x-.475f)*.014f*p.breath*chest
        py-=.0045f*p.breath*chest
        // Head, hat and ears share a parent neck motion, with soft shoulder blending.
        val head=smoothWeight(((y-.38f)/.23f).coerceAtLeast(0f)) *
            smoothWeight(((abs(x-.52f)-.22f)/.15f).coerceAtLeast(0f))
        rotate(.48f,.51f,p.head,head)
        py-=.0025f*p.breath*head
        // Ear tips lag behind the head. Ear roots remain attached under the beret.
        val leftEar=oval(x,y,.56f,.105f,.14f,.17f)
        val rightEar=oval(x,y,.735f,.18f,.17f,.11f)
        rotate(.48f,.205f,p.ear,leftEar)
        rotate(.655f,.255f,-p.ear*.8f,rightEar)
        // Three nested pivots: shoulder -> elbow -> wrist, not a floating hand.
        val arm=capsule(x,y,.335f,.48f,.204f,.36f,.07f)
        val forearm=capsule(x,y,.28f,.445f,.204f,.36f,.058f)
        val hand=oval(x,y,.205f,.363f,.062f,.068f)
        rotate(.335f,.48f,p.shoulder,arm)
        rotate(.28f,.445f,p.elbow,forearm)
        rotate(.235f,.40f,p.wrist,hand)
        // Cloth follows breathing rather than moving as a rigid slab.
        py+=.002f*p.breath*oval(x,y,.36f,.68f,.09f,.10f)
        // Hip, knee and ankle follow opposite phases during the in-place stepping action.
        val leftLeg=oval(x,y,.35f,.82f,.20f,.19f)
        val rightLeg=oval(x,y,.56f,.86f,.13f,.17f)
        rotate(.45f,.74f,.14f*p.leftStep-.045f*p.crouch,leftLeg)
        rotate(.35f,.81f,-.10f*p.leftStep+.06f*p.crouch,oval(x,y,.29f,.86f,.13f,.13f))
        rotate(.53f,.76f,.12f*p.rightStep+.045f*p.crouch,rightLeg)
        rotate(.57f,.85f,-.08f*p.rightStep-.06f*p.crouch,oval(x,y,.56f,.92f,.10f,.08f))
        // Compression is strongest above the planted feet; takeoff then follows a ballistic arc.
        val rabbit=smoothWeight(((x-.64f)/.13f).coerceAtLeast(0f))
        py+=.055f*p.crouch*(.94f-y).coerceAtLeast(0f)*rabbit
        px+=(x-.48f)*.018f*p.crouch*chest
        py+=p.bodyBob*rabbit-p.jumpHeight
        result[index++]=px; result[index++]=py
    }
    return result
}
