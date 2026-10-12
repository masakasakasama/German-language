package com.masakasakasama.reisedeutsch.learning

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.masakasakasama.reisedeutsch.data.PhraseCard

class SchedulerTest {
    private val day=LocalDate.parse("2026-10-12")
    private val now="2026-10-12T12:00:00Z"

    @Test fun spacedIntervalsMatchExistingApplication(){
        var state:CardSchedule?=null
        val actual=mutableListOf<Int>()
        repeat(5){state=Scheduler.applyAssessment(state,Assessment.OK,day,now);actual+=state!!.intervalDays}
        assertEquals(listOf(1,3,7,14,35),actual)
    }
    @Test fun knowAndDontKnowKeepTheirDistinctSchedulingRules(){
        val known=Scheduler.applyAssessment(null,Assessment.KNOW,day,now)
        assertEquals(7,known.intervalDays);assertEquals(1,known.reps);assertEquals(day.plusDays(7),known.dueLocalDate)
        val old=known.copy(reps=3,ease=2.2)
        val forgotten=Scheduler.applyAssessment(old,Assessment.DONT_KNOW,day,now)
        assertEquals(1,forgotten.intervalDays);assertEquals(old.reps,forgotten.reps);assertEquals(old.lapses+1,forgotten.lapses)
        assertEquals(2.0,forgotten.ease,0.00001)
    }
    @Test fun incorrectQuizIsDueTodayAndCanStillBeClassedAsWeak(){
        val missed=Scheduler.review(CardSchedule(dueLocalDate=day),ReviewGrade.AGAIN,day,now)
        assertEquals(0,missed.intervalDays);assertEquals(0,missed.reps);assertEquals(day,missed.dueLocalDate)
        assertTrue(missed.lapses>0)
        assertEquals(3.8,Scheduler.weakness(missed,day),0.00001)
    }
    @Test fun localCalendarDaysDoNotUseTwentyFourHourArithmetic(){
        val start=LocalDate.parse("2026-03-29")
        assertEquals(LocalDate.parse("2026-03-30"),Scheduler.applyAssessment(null,Assessment.OK,start,now).dueLocalDate)
    }
    @Test fun masteryIsAReviewLabelNotAnAssessment(){
        assertEquals("学習中",Scheduler.summary(CardSchedule(dueLocalDate=day)))
        assertEquals("定着目安",Scheduler.summary(CardSchedule(intervalDays=21,dueLocalDate=day)))
    }
    @Test fun typedAnswersNormalizeUnicodeSpacingAndFinalPunctuation(){
        val phrase=PhraseCard("id","Ich hätte gern einen Kaffee, bitte.","コーヒーをください","cafe","PHRASE",acceptedAnswers=listOf("Ich hätte gern einen Kaffee, bitte."))
        assertTrue(AnswerGrader.matches(phrase,"  ICH  HÄTTE GERN EINEN KAFFEE, BITTE!  "))
        assertTrue(!AnswerGrader.matches(phrase,"Ich hätte gern Kaffee, bitte."))
        val vocab=PhraseCard("noun","der Bahnhof","駅","station","VOCAB",acceptedAnswers=listOf("der Bahnhof"))
        assertTrue(!AnswerGrader.matches(vocab,"Der Bahnhof"))
    }
}
