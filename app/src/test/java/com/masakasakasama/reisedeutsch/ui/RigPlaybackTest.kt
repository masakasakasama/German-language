package com.masakasakasama.reisedeutsch.ui
import org.junit.Assert.*
import org.junit.Test
class RigPlaybackTest {
    @Test fun walkLoopsAtDuplicateLastKey() {
        assertEquals(Triple(0,1,0f),rigFrame(1f,30,31,true))
        assertEquals(Triple(15,16,0f),rigFrame(1.5f,30,31,true))
    }
    @Test fun nonLoopingJumpHoldsLastValidKey() {
        assertEquals(Triple(42,42,0f),rigFrame(3f,30,43,false))
        val p=rigFrame(.05f,30,43,false)
        assertEquals(1,p.first);assertEquals(2,p.second);assertEquals(.5f,p.third,.0001f)
    }
}
