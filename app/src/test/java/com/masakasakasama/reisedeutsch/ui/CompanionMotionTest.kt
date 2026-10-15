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
    @Test fun jointsEaseInAndOut() {
        assertEquals(0f, companionPose(0f, 0f).wrist, .0001f)
        assertEquals(0f, companionPose(0f, 2.4f).wrist, .0001f)
        assertTrue(kotlin.math.abs(companionPose(0f, 1.05f).wrist) > .1f)
        assertTrue(companionPose(.9f).breath > .99f)
    }
    @Test fun feetStayPlantedWhileChestAndHandMove() {
        val rest = deformCompanionMesh(CompanionPose())
        val moved = deformCompanionMesh(CompanionPose(1f,.012f,.025f,.035f,.10f,.20f))
        fun at(x:Int,y:Int) = (y*49+x)*2
        for ((x,y) in listOf(27 to 44, 39 to 40)) {
            val i=at(x,y)
            assertEquals(rest[i],moved[i],.00001f)
            assertEquals(rest[i+1],moved[i+1],.00001f)
        }
        assertTrue(kotlin.math.abs(rest[at(23,29)+1]-moved[at(23,29)+1]) > .003f)
        assertTrue(kotlin.math.abs(rest[at(10,18)]-moved[at(10,18)]) > .002f)
    }
    @Test fun steppingAlternatesAndReturnsToRest() {
        val step=companionPose(0f,action=CompanionAction.WALK,actionAge=1.25f)
        assertTrue(step.leftStep > .5f)
        assertEquals(-step.leftStep,step.rightStep,.0001f)
        val rest=companionPose(0f,action=CompanionAction.WALK,actionAge=4f)
        assertEquals(0f,rest.leftStep,.0001f)
    }
    @Test fun jumpCompressesTakesOffAndAbsorbsLanding() {
        fun jump(t:Float)=companionPose(0f,action=CompanionAction.JUMP,actionAge=t)
        assertTrue(jump(.175f).crouch > .9f)
        assertEquals(0f,jump(.175f).jumpHeight,.0001f)
        assertEquals(.1f,jump(.65f).jumpHeight,.0001f)
        assertTrue(jump(1.15f).crouch > .6f)
        assertEquals(0f,jump(1.35f).jumpHeight,.0001f)
        assertEquals(0f,jump(1.35f).crouch,.0001f)
    }
    @Test fun meshNeverFoldsAndExportActualAnimation() {
        val poses=(0..216).map { companionPose(it/24f) } +
            (0..96).map { companionPose(9f+it/24f,action=CompanionAction.WALK,actionAge=it/24f) } +
            (0..40).map { companionPose(13f+it/24f,action=CompanionAction.JUMP,actionAge=it/24f) }
        val frames=poses.map { deformCompanionMesh(it) }
        for (mesh in frames) for (y in 0 until 48) for(x in 0 until 48) {
            val a=(y*49+x)*2; val b=a+2; val c=a+98; val d=c+2
            fun area(i:Int,j:Int,k:Int)=(mesh[j]-mesh[i])*(mesh[k+1]-mesh[i+1])-(mesh[j+1]-mesh[i+1])*(mesh[k]-mesh[i])
            assertTrue("Mesh triangle folded",area(a,b,c)>0f)
            assertTrue("Mesh triangle folded",area(b,d,c)>0f)
        }
        val out=java.io.File("build/motion-preview.json")
        out.parentFile.mkdirs()
        out.writeText("{\"columns\":48,\"rows\":48,\"fps\":24,\"frames\":["+frames.joinToString(",") { it.joinToString(",","[","]") }+"]}")
    }
}
