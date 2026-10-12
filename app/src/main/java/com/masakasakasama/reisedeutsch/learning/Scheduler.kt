package com.masakasakasama.reisedeutsch.learning

import java.time.LocalDate

enum class Assessment { DONT_KNOW, OK, KNOW }
enum class ReviewGrade { AGAIN, GOOD, HARD, EASY }
data class CardSchedule(val ease: Double=2.5, val intervalDays: Int=0, val dueLocalDate: LocalDate, val reps: Int=0, val lapses: Int=0, val lastReviewedAt: String?=null)

object Scheduler {
    const val DEFAULT_EASE=2.5
    const val MIN_EASE=1.3
    private val steps=intArrayOf(1,3,7,14)
    fun jsRound(n: Double)=kotlin.math.floor(n+0.5).toInt()

    fun applyAssessment(state: CardSchedule?, assessment: Assessment, today: LocalDate, now: String): CardSchedule {
        val s=state ?: CardSchedule(dueLocalDate=today)
        val e=s.ease.coerceIn(MIN_EASE,3.0)
        return when(assessment) {
            Assessment.DONT_KNOW -> CardSchedule(maxOf(MIN_EASE,e-0.2),1,today.plusDays(1),s.reps,s.lapses+1,now)
            Assessment.OK -> {
                val days=if(s.reps<steps.size) steps[s.reps.coerceAtLeast(0)] else jsRound(s.intervalDays*e)
                CardSchedule(e,days,today.plusDays(days.toLong()),s.reps+1,s.lapses,now)
            }
            Assessment.KNOW -> {
                val days=maxOf(7,jsRound((s.intervalDays.takeIf{it>0}?:1)*2.0))
                CardSchedule(minOf(3.0,e+0.15),days,today.plusDays(days.toLong()),s.reps+1,s.lapses,now)
            }
        }
    }

    fun review(state: CardSchedule?, grade: ReviewGrade, today: LocalDate, now: String): CardSchedule {
        val s=state?:CardSchedule(dueLocalDate=today);val e=s.ease.coerceIn(MIN_EASE,3.0)
        return when(grade){
            ReviewGrade.AGAIN->CardSchedule(maxOf(MIN_EASE,e-0.2),0,today,0,s.lapses+1,now)
            ReviewGrade.HARD->{val d=steps[s.reps.coerceIn(0,steps.lastIndex)];CardSchedule(maxOf(MIN_EASE,e-0.15),d,today.plusDays(d.toLong()),s.reps+1,s.lapses,now)}
            ReviewGrade.GOOD->{val d=if(s.reps<steps.size)steps[s.reps.coerceAtLeast(0)] else jsRound(s.intervalDays*e);CardSchedule(e,d,today.plusDays(d.toLong()),s.reps+1,s.lapses,now)}
            ReviewGrade.EASY->{val i=s.reps+2;val d=if(i<steps.size)steps[i] else jsRound(s.intervalDays*e*1.3);CardSchedule(minOf(3.0,e+0.15),d,today.plusDays(d.toLong()),s.reps+1,s.lapses,now)}
        }
    }

    fun weakness(s:CardSchedule,today:LocalDate):Double {
        val overdue=(today.toEpochDay()-s.dueLocalDate.toEpochDay()).coerceAtLeast(0).toInt()
        return s.lapses*3.0+maxOf(0.0,DEFAULT_EASE-s.ease)*4+(if(s.intervalDays<=1&&s.reps>0)2.0 else 0.0)+minOf(overdue,7)*0.5
    }
    fun summary(s:CardSchedule)=when{ s.intervalDays==0->"学習中";s.intervalDays>=21->"定着目安";else->"復習中" }
}
