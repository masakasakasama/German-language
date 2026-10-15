package com.masakasakasama.reisedeutsch.ui

import kotlin.math.*

data class CompanionPose(val breath: Float = 0f, val head: Float = 0f, val ear: Float = 0f,
                         val shoulder: Float = 0f, val elbow: Float = 0f, val wrist: Float = 0f)
fun companionRuns(active: Boolean, paused: Boolean, systemMotion: Boolean, manualPlay: Boolean) =
    active && !paused && (systemMotion || manualPlay)

/** Continuous breathing plus a softly eased two-second greeting, never whole-image rotation. */
fun companionPose(seconds: Float, waveAge: Float = -1f): CompanionPose {
    val phase = seconds * (2f * PI.toFloat() / 3.6f)
    val autoAge = seconds % 9f - 2f
    val age = if (waveAge >= 0f) waveAge else autoAge
    val wave = if (age in 0f..2.4f) {
        val envelope = sin(PI.toFloat() * age / 2.4f).pow(2)
        envelope * sin(age * 2f * PI.toFloat() * 1.65f)
    } else 0f
    return CompanionPose(sin(phase), .012f * sin(phase * .5f),
        .024f * (sin(phase * .5f - .45f) + sin(.45f)),
        .035f * wave, .10f * wave, .20f * wave)
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
        result[index++]=px; result[index++]=py
    }
    return result
}
