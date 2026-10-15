package com.masakasakasama.reisedeutsch.ui

import org.junit.Assert.*
import org.junit.Test

class CompanionMotionTest {
    @Test fun manualPlayWorksWhenSystemAnimationsAreDisabled() {
        assertFalse(companionRuns(true, false, false, false))
        assertTrue(companionRuns(true, false, false, true))
    }
    @Test fun pauseAndBackgroundAlwaysStopEvenManualPlayback() {
        assertFalse(companionRuns(false, false, true, true))
        assertFalse(companionRuns(true, true, true, true))
    }
    @Test fun idleFramesVisiblyDifferAndLoop() {
        assertTrue(companionPose(.6f, false).lift > 7f)
        assertTrue(companionPose(1.8f, false).lift < -7f)
        assertEquals(companionPose(0f, false).lift, companionPose(2.4f, false).lift, .001f)
    }
    @Test fun tapHasExtraBounceAndSettles() {
        assertTrue(companionPose(.3f, true).lift > companionPose(.3f, false).lift + 15f)
        assertEquals(companionPose(1.3f, false), companionPose(1.3f, true))
    }
}
