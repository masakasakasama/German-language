package com.masakasakasama.reisedeutsch.ui

/** The final key closes loop clips, so looping uses frames - 1 intervals. */
fun rigFrame(seconds:Float,fps:Int,frames:Int,loop:Boolean):Triple<Int,Int,Float> {
    require(fps>0 && frames>1)
    val raw=seconds.coerceAtLeast(0f)*fps
    val f=if(loop)raw%(frames-1) else raw.coerceAtMost((frames-1).toFloat())
    val first=f.toInt();return Triple(first,(first+1).coerceAtMost(frames-1),f-first)
}
