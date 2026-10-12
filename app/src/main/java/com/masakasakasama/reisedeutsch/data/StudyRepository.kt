package com.masakasakasama.reisedeutsch.data

import androidx.room.withTransaction
import com.masakasakasama.reisedeutsch.learning.Assessment
import com.masakasakasama.reisedeutsch.learning.CardSchedule
import com.masakasakasama.reisedeutsch.learning.Scheduler
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class DashboardStats(val due:Int,val weak:Int,val xp:Int,val learned:Int,val mastered:Int,val streak:Int,val today:Int,val doneLessons:Int)

class StudyRepository(val db:StudyDatabase,val content:ContentPack,private val clock:Clock=Clock.systemDefaultZone()){
    val dao=db.dao()
    val states=dao.watchCards()
    val events=dao.watchEvents()
    val stats:Flow<DashboardStats> = combine(states,events){cards,attempts->
        val now=LocalDate.now(clock)
        val known=cards.associateBy{it.cardId}
        val learned=cards.filter{it.learnedAt!=null}.map{it.cardId}.toSet()
        val due=cards.count{LocalDate.parse(it.dueLocalDate)<=now}
        val weak=cards.count{it.lapses>0&&Scheduler.weakness(it.schedule(),now)>=3}
        val dates=attempts.map{it.localDate}.filter{it.isNotBlank()}.toSet()
        var streak=0;var date=now
        while(date.toString() in dates){streak++;date=date.minusDays(1)}
        val todayEvents=attempts.filter{it.localDate==now.toString()}
        DashboardStats(due,weak,dao.totalXp(),learned.size,cards.count{it.intervalDays>=21},streak,todayEvents.map{it.cardId}.distinct().size,0)
    }

    private fun CardStateEntity.schedule()=CardSchedule(ease,intervalDays,LocalDate.parse(dueLocalDate),reps,lapses,lastReviewedAt)
    private fun CardSchedule.entity(cardId:String,assessment:String?,learnedAt:String?,collectedAt:String?,zone:ZoneId)=CardStateEntity(cardId,assessment,learnedAt,collectedAt,ease,intervalDays,dueLocalDate.toString(),zone.id,reps,lapses,lastReviewedAt)
    private fun date()=LocalDate.now(clock)
    private fun timestamp()=clock.instant().toString()
    private fun zone()=clock.zone

    suspend fun assess(eventId:String,sessionId:String,step:Int,card:PhraseCard,value:Assessment){
        val day=date();val now=timestamp();val zone=zone()
        db.withTransaction {
            if(dao.hasEvent(eventId))return@withTransaction
            val old=dao.card(card.id)
            val new=Scheduler.applyAssessment(old?.schedule(),value,day,now)
            val mark=value.name
            val first=old?.collectedAt==null
            val xp=2+(if(first)2 else 0)
            dao.putCard(new.entity(card.id,mark,old?.learnedAt?:now,old?.collectedAt?:now,zone))
            dao.putEvent(AnswerEventEntity(eventId,sessionId,step,card.id,"ASSESSMENT",mark,null,day.toString(),now,xp))
            dao.putReward(RewardGrantEntity("assessment:${card.id}:$day",day.toString(),2,"ASSESSMENT"))
            if(first)dao.putReward(RewardGrantEntity("collect:${card.id}",day.toString(),2,"COLLECT"))
        }
    }

    suspend fun answer(eventId:String,sessionId:String,step:Int,card:PhraseCard,correct:Boolean){
        val day=date();val now=timestamp();val zone=zone()
        db.withTransaction {
            if(dao.hasEvent(eventId))return@withTransaction
            val old=dao.card(card.id);val firstToday=dao.events().none{it.cardId==card.id&&it.localDate==day.toString()&&it.kind=="EXERCISE"}
            val grade=if(correct)com.masakasakasama.reisedeutsch.learning.ReviewGrade.GOOD else com.masakasakasama.reisedeutsch.learning.ReviewGrade.AGAIN
            val computed=Scheduler.review(old?.schedule(),grade,day,now)
            val sameDayAssessment=old?.let{cardState->cardState.assessment!=null&&cardState.lastReviewedAt?.let{at->runCatching{Instant.parse(at).atZone(ZoneId.of(cardState.scheduleZone)).toLocalDate()==day}.getOrDefault(false)}==true}==true
            val schedule=if(correct&&sameDayAssessment&&old!=null&&computed.intervalDays<old.intervalDays)computed.copy(intervalDays=old.intervalDays,dueLocalDate=day.plusDays(old.intervalDays.toLong()))else computed
            val xp=if(correct&&firstToday)5 else 0
            dao.putCard(schedule.entity(card.id,old?.assessment,old?.learnedAt?:now,old?.collectedAt?:now,zone))
            dao.putEvent(AnswerEventEntity(eventId,sessionId,step,card.id,"EXERCISE",null,correct,day.toString(),now,xp))
            if(xp>0)dao.putReward(RewardGrantEntity("exercise:${card.id}:$day",day.toString(),xp,"EXERCISE"))
        }
    }

    suspend fun markLessonStep(lesson:Lesson,cardId:String,step:Int){
        val old=dao.lesson(lesson.id)
        val done=old?.completedSteps.orEmpty().split(',').filter{it.isNotBlank()}.toMutableSet().apply{add("$step:$cardId")}
        val completed=done.size>=6
        db.withTransaction {
            dao.putLesson(LessonProgressEntity(lesson.id,if(completed)old?.completedAt?:timestamp() else old?.completedAt,done.joinToString(",")))
            if(completed&&old?.completedAt==null)dao.putReward(RewardGrantEntity("lesson:${lesson.id}",date().toString(),10,"LESSON"))
        }
    }

    suspend fun saveSession(lesson:Lesson,cursor:Int){
        val now=timestamp();val old=dao.session();val id=old?.sessionId?:"active"
        if(cursor>=6)db.withTransaction{dao.putSession(SessionEntity(id,lesson.id,cursor,old?.createdAt?:now,now));dao.clearSession()}
        else dao.putSession(SessionEntity(id,lesson.id,cursor,old?.createdAt?:now,now))
    }
    suspend fun session()=dao.session()
    suspend fun dueCards():List<PhraseCard>{val d=date();val state=dao.cards().filter{LocalDate.parse(it.dueLocalDate)<=d}.map{it.cardId}.toSet();return content.cards.filter{it.id in state&&it.kind=="PHRASE"}}
    suspend fun weakCards():List<PhraseCard>{val d=date();val s=dao.cards().filter{it.lapses>0&&Scheduler.weakness(it.schedule(),d)>=3}.map{it.cardId}.toSet();return content.cards.filter{it.id in s&&it.kind=="PHRASE"}}
    suspend fun totalXp()=dao.totalXp()
    suspend fun reset(){db.withTransaction{dao.clearCards();dao.clearEvents();dao.clearLessons();dao.clearRewards();dao.clearSessions()}}
    suspend fun exportJson():String{
        val root=org.json.JSONObject().put("format","reisedeutsch-backup").put("schemaVersion",1).put("contentVersion","1.0.0").put("exportedAt",timestamp())
        fun rows(name:String, values:List<Any>):org.json.JSONArray{val a=org.json.JSONArray();values.forEach{v->val j=org.json.JSONObject();when(v){is CardStateEntity->j.put("cardId",v.cardId).put("assessment",v.assessment).put("learnedAt",v.learnedAt).put("collectedAt",v.collectedAt).put("ease",v.ease).put("intervalDays",v.intervalDays).put("dueLocalDate",v.dueLocalDate).put("scheduleZone",v.scheduleZone).put("reps",v.reps).put("lapses",v.lapses).put("lastReviewedAt",v.lastReviewedAt);is AnswerEventEntity->j.put("eventId",v.eventId).put("sessionId",v.sessionId).put("step",v.step).put("cardId",v.cardId).put("kind",v.kind).put("assessment",v.assessment).put("correct",v.correct).put("localDate",v.localDate).put("atUtc",v.atUtc).put("xp",v.xp);is LessonProgressEntity->j.put("lessonId",v.lessonId).put("completedAt",v.completedAt).put("completedSteps",v.completedSteps);is RewardGrantEntity->j.put("rewardKey",v.rewardKey).put("localDate",v.localDate).put("xp",v.xp).put("kind",v.kind)};a.put(j)};root.put(name,a);return a}
        rows("cards",dao.cards());rows("events",dao.events());rows("lessons",dao.lessons());rows("rewards",dao.rewards())
        return root.toString(2)
    }
    suspend fun importJson(raw:String){
        require(raw.length<=20*1024*1024)
        val j=org.json.JSONObject(raw);require(j.getString("format")=="reisedeutsch-backup"&&j.getInt("schemaVersion")==1)
        val cards=j.getJSONArray("cards");val events=j.getJSONArray("events");val lessons=j.getJSONArray("lessons");val rewards=j.getJSONArray("rewards")
        require(cards.length()<=10000&&events.length()<=100000&&lessons.length()<=1000)
        val parsedCards=(0 until cards.length()).map{i->val x=cards.getJSONObject(i);require(content.card(x.getString("cardId"))!=null);val ease=x.getDouble("ease");val iv=x.getInt("intervalDays");val reps=x.getInt("reps");val lapses=x.getInt("lapses");require(ease in 1.3..3.0&&iv>=0&&reps>=0&&lapses>=0);CardStateEntity(x.getString("cardId"),x.optString("assessment").takeIf{it!="null"},x.optString("learnedAt").takeIf{it!="null"},x.optString("collectedAt").takeIf{it!="null"},ease,iv,x.getString("dueLocalDate"),x.getString("scheduleZone"),reps,lapses,x.optString("lastReviewedAt").takeIf{it!="null"})}
        val parsedEvents=(0 until events.length()).map{i->val x=events.getJSONObject(i);require(content.card(x.getString("cardId"))!=null);AnswerEventEntity(x.getString("eventId"),x.getString("sessionId"),x.getInt("step"),x.getString("cardId"),x.getString("kind"),x.optString("assessment").takeIf{it!="null"},if(x.isNull("correct"))null else x.getBoolean("correct"),x.getString("localDate"),x.getString("atUtc"),x.getInt("xp"))}
        val parsedLessons=(0 until lessons.length()).map{i->val x=lessons.getJSONObject(i);require(content.lesson(x.getString("lessonId"))!=null);LessonProgressEntity(x.getString("lessonId"),x.optString("completedAt").takeIf{it!="null"},x.getString("completedSteps"))}
        val parsedRewards=(0 until rewards.length()).map{i->val x=rewards.getJSONObject(i);RewardGrantEntity(x.getString("rewardKey"),x.getString("localDate"),x.getInt("xp"),x.getString("kind"))}
        db.withTransaction{dao.clearCards();dao.clearEvents();dao.clearLessons();dao.clearRewards();dao.clearSessions();parsedCards.forEach{dao.putCard(it)};parsedEvents.forEach{dao.putEvent(it)};parsedLessons.forEach{dao.putLesson(it)};parsedRewards.forEach{dao.putReward(it)}}
    }
}
