package com.masakasakasama.reisedeutsch.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName="card_state")
data class CardStateEntity(
    @PrimaryKey val cardId:String,
    val assessment:String?,
    val learnedAt:String?,
    val collectedAt:String?,
    val ease:Double,
    val intervalDays:Int,
    val dueLocalDate:String,
    val scheduleZone:String,
    val reps:Int,
    val lapses:Int,
    val lastReviewedAt:String?,
)
@Entity(tableName="answer_event",indices=[Index("cardId"),Index("localDate")])
data class AnswerEventEntity(
    @PrimaryKey val eventId:String,val sessionId:String,val step:Int,val cardId:String,val kind:String,
    val assessment:String?,val correct:Boolean?,val localDate:String,val atUtc:String,val xp:Int,
)
@Entity(tableName="lesson_progress")
data class LessonProgressEntity(@PrimaryKey val lessonId:String,val completedAt:String?,val completedSteps:String)
@Entity(tableName="study_session")
data class SessionEntity(@PrimaryKey val sessionId:String,val lessonId:String,val cursor:Int,val createdAt:String,val updatedAt:String)
@Entity(tableName="reward_grant")
data class RewardGrantEntity(@PrimaryKey val rewardKey:String,val localDate:String,val xp:Int,val kind:String)

@Dao interface StudyDao {
    @Query("SELECT * FROM card_state ORDER BY cardId") fun watchCards():Flow<List<CardStateEntity>>
    @Query("SELECT * FROM card_state ORDER BY cardId") suspend fun cards():List<CardStateEntity>
    @Query("SELECT * FROM card_state WHERE cardId=:id") suspend fun card(id:String):CardStateEntity?
    @Upsert suspend fun putCard(c:CardStateEntity)
    @Query("SELECT * FROM answer_event ORDER BY atUtc") fun watchEvents():Flow<List<AnswerEventEntity>>
    @Query("SELECT * FROM answer_event ORDER BY atUtc DESC LIMIT 200") suspend fun recentEvents():List<AnswerEventEntity>
    @Query("SELECT EXISTS(SELECT 1 FROM answer_event WHERE eventId=:id)") suspend fun hasEvent(id:String):Boolean
    @Query("SELECT COUNT(*) FROM answer_event WHERE localDate=:date") suspend fun countEventsToday(date:String):Int
    @Query("SELECT COALESCE(SUM(xp),0) FROM reward_grant") suspend fun totalXp():Int
    @Query("SELECT * FROM lesson_progress WHERE lessonId=:id") suspend fun lesson(id:String):LessonProgressEntity?
    @Upsert suspend fun putLesson(p:LessonProgressEntity)
    @Query("SELECT * FROM lesson_progress") suspend fun lessons():List<LessonProgressEntity>
    @Query("SELECT * FROM study_session LIMIT 1") suspend fun session():SessionEntity?
    @Upsert suspend fun putSession(s:SessionEntity)
    @Query("DELETE FROM study_session") suspend fun clearSession()
    @Query("SELECT * FROM reward_grant") suspend fun rewards():List<RewardGrantEntity>
    @Query("SELECT * FROM answer_event") suspend fun events():List<AnswerEventEntity>
    @Query("DELETE FROM card_state") suspend fun clearCards()
    @Query("DELETE FROM answer_event") suspend fun clearEvents()
    @Query("DELETE FROM lesson_progress") suspend fun clearLessons()
    @Query("DELETE FROM reward_grant") suspend fun clearRewards()
    @Query("DELETE FROM study_session") suspend fun clearSessions()
    @Upsert suspend fun putEvent(e:AnswerEventEntity)
    @Upsert suspend fun putReward(r:RewardGrantEntity)
}

@Database(entities=[CardStateEntity::class,AnswerEventEntity::class,LessonProgressEntity::class,SessionEntity::class,RewardGrantEntity::class],version=1,exportSchema=true)
abstract class StudyDatabase:RoomDatabase(){abstract fun dao():StudyDao;companion object{
    @Volatile private var INSTANCE:StudyDatabase?=null
    fun get(context:Context):StudyDatabase=INSTANCE?:synchronized(this){INSTANCE?:Room.databaseBuilder(context.applicationContext,StudyDatabase::class.java,"reisedeutsch.db").build().also{INSTANCE=it}}
}}
