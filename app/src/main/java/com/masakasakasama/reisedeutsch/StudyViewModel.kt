package com.masakasakasama.reisedeutsch

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.masakasakasama.reisedeutsch.data.*
import com.masakasakasama.reisedeutsch.learning.Assessment
import com.masakasakasama.reisedeutsch.learning.CardSchedule
import com.masakasakasama.reisedeutsch.learning.Scheduler
import com.masakasakasama.reisedeutsch.update.ReleaseInfo
import com.masakasakasama.reisedeutsch.update.UpdateRepository
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SessionUi(val id:String,val lesson:Lesson?,val cards:List<PhraseCard>,val cursor:Int=0,val revealed:Boolean=false,val answered:Boolean=false,val selected:Int?=null,val fromReview:Boolean=false)
data class AppStats(val due:Int=0,val weak:Int=0,val xp:Int=0,val learned:Int=0,val mastered:Int=0,val streak:Int=0,val today:Int=0,val completedLessons:Set<String> = emptySet(),val collected:Set<String> = emptySet(),val states:Map<String,CardSchedule> = emptyMap(),val assessments:Map<String,String> = emptyMap(),val loading:Boolean=true)
data class UpdateUi(val checking:Boolean=false,val downloading:Boolean=false,val release:ReleaseInfo?=null,val apkPath:String?=null,val error:String?=null)

class StudyViewModel(application:Application):AndroidViewModel(application){
    val content=application.assets.open("content-v1.json").bufferedReader().use{ContentPack.parse(it.readText())}
    val repository=StudyRepository(StudyDatabase.get(application),content)
    private val _page=MutableStateFlow("home");val page=_page.asStateFlow()
    private val _session=MutableStateFlow<SessionUi?>(null);val session=_session.asStateFlow()
    private val _notice=MutableStateFlow<String?>(null);val notice=_notice.asStateFlow()
    private val _stats=MutableStateFlow(AppStats());val stats=_stats.asStateFlow()
    private val _filter=MutableStateFlow("ALL");val filter=_filter.asStateFlow()
    private val preferences=application.getSharedPreferences("reisedeutsch-settings",Application.MODE_PRIVATE)
    private val _dark=MutableStateFlow(preferences.getBoolean("dark",false));val dark=_dark.asStateFlow()
    private val _goal=MutableStateFlow(preferences.getInt("goal",10));val goal=_goal.asStateFlow()
    private val _conversation=MutableStateFlow<Conversation?>(null);val conversation=_conversation.asStateFlow()
    private val _update=MutableStateFlow(UpdateUi());val update=_update.asStateFlow()
    private val _spoken=MutableStateFlow("");val spoken=_spoken.asStateFlow()
    init{viewModelScope.launch{
        repository.session()?.let{saved->content.lesson(saved.lessonId)?.let{lesson->_session.value=SessionUi(saved.sessionId,lesson,lesson.cardIds.mapNotNull(content::card),saved.cursor.coerceIn(0,5));_page.value="study"}}
        combine(repository.states,repository.events){a,b->a to b}.collect{(cards,events)->
            val today=LocalDate.now();val byId=cards.associateBy{it.cardId};val learned=cards.filter{it.learnedAt!=null};val done=repository.dao.lessons().filter{it.completedAt!=null}.map{it.lessonId}.toSet()
            val dates=events.map{it.localDate}.toSet();var streak=0;var day=today;while(day.toString() in dates){streak++;day=day.minusDays(1)}
            val due=learned.count{LocalDate.parse(it.dueLocalDate)<=today};val weak=learned.count{it.lapses>0&&Scheduler.weakness(it.asSchedule(),today)>=3}
            val xp=repository.totalXp();_stats.value=AppStats(due,weak,xp,learned.size,cards.count{it.intervalDays>=21},streak,events.filter{it.localDate==today.toString()}.map{it.cardId}.distinct().size,done,learned.map{it.cardId}.toSet(),byId.mapValues{it.value.asSchedule()},byId.mapNotNull{(id,s)->s.assessment?.let{id to it}}.toMap(),false)
        }
    }}
    private fun CardStateEntity.asSchedule()=CardSchedule(ease,intervalDays,LocalDate.parse(dueLocalDate),reps,lapses,lastReviewedAt)
    fun setPage(value:String){_page.value=value;_conversation.value=null}
    fun setFilter(value:String){_filter.value=value};fun setDark(value:Boolean){_dark.value=value;preferences.edit().putBoolean("dark",value).apply()};fun setGoal(value:Int){if(value in listOf(5,10,20)){_goal.value=value;preferences.edit().putInt("goal",value).apply()}}
    fun toast(value:String){_notice.value=value};fun clearNotice(){_notice.value=null}
    fun dueClick(){viewModelScope.launch{val due=repository.dueCards();if(due.isEmpty())toast("今日の復習はありません ☀️")else startReview(due)}}
    fun weakClick(){viewModelScope.launch{val weak=repository.weakCards();if(weak.isEmpty())toast("苦手カードはありません 🐰")else startReview(weak)}}
    private fun startReview(cards:List<PhraseCard>){_session.value=SessionUi("review-${System.currentTimeMillis()}",null,cards,fromReview=true);_page.value="study"}
    fun startLesson(lesson:Lesson){_session.value=SessionUi("session-${lesson.id}-${System.currentTimeMillis()}",lesson,lesson.cardIds.mapNotNull(content::card));_page.value="study"}
    fun startCard(card:PhraseCard){_session.value=SessionUi("card-${card.id}-${System.currentTimeMillis()}",null,listOf(card),fromReview=true);_page.value="study"}
    fun startConversation(c:Conversation){_conversation.value=c;_page.value="conversation-detail"}
    fun setSpoken(value:String){_spoken.value=value};fun clearSpoken(){_spoken.value=""}
    fun grade(card:PhraseCard,answer:String):Boolean=com.masakasakasama.reisedeutsch.learning.AnswerGrader.matches(card,answer)
    fun saveConversationAnswer(conversationId:String,cardId:String,answer:String){val card=content.card(cardId)?:return;if(answer.isBlank())return;viewModelScope.launch{runCatching{repository.answer("conversation:$conversationId:$cardId",conversationId,0,card,grade(card,answer))}.onFailure{toast("保存できませんでした")}}}
    fun reveal(){_session.value=_session.value?.copy(revealed=true)}
    fun choose(index:Int){_session.value=_session.value?.copy(selected=index)}
    fun saveAssessment(value:Assessment){val s=_session.value?:return;val card=s.cards.getOrNull(s.cursor/2)?:return
        viewModelScope.launch{runCatching{repository.assess("${s.id}:${s.cursor}:assessment",s.id,s.cursor,card,value);if(s.lesson!=null)repository.markLessonStep(s.lesson,card.id,s.cursor)}.onSuccess{_session.value=_session.value?.copy(answered=true);toast("次回の復習を設定しました 🐰")}.onFailure{toast("保存できませんでした。もう一度お試しください")}}
    }
    fun saveAnswer(correct:Boolean){val s=_session.value?:return;val card=s.cards.getOrNull(s.cursor/2)?:return
        viewModelScope.launch{runCatching{repository.answer("${s.id}:${s.cursor}:exercise",s.id,s.cursor,card,correct);if(s.lesson!=null)repository.markLessonStep(s.lesson,card.id,s.cursor)}.onSuccess{_session.value=_session.value?.copy(answered=true);if(!correct)toast("大丈夫。答えを見て、もう一度やってみよう")}.onFailure{toast("保存できませんでした。もう一度お試しください")}}
    }
    fun next(){val s=_session.value?:return
        if(s.cursor>=s.cards.size*2-1){viewModelScope.launch{if(s.lesson!=null)repository.saveSession(s.lesson,6)};_session.value=null;_page.value=if(s.fromReview)"home" else "complete";return}
        val next=s.cursor+1;_session.value=s.copy(cursor=next,revealed=false,answered=false,selected=null);if(s.lesson!=null)viewModelScope.launch{repository.saveSession(s.lesson,next)}
    }
    fun finishReview(){_session.value=null;_page.value="home"}
    fun options(card:PhraseCard):List<PhraseCard>{val others=content.cards.filter{it.kind=="PHRASE"&&it.id!=card.id&&it.scene==card.scene};return(listOf(card)+others.shuffled().take(3)).shuffled()}
    fun nextDue(card:PhraseCard,value:Assessment):String{val date=LocalDate.now();val prev=_stats.value.states[card.id];return Scheduler.applyAssessment(prev,value,date,ZonedDateTime.now().toInstant().toString()).dueLocalDate.toString()}
    fun checkUpdate(){
        _update.value=UpdateUi(checking=true)
        viewModelScope.launch{runCatching{UpdateRepository.check(com.masakasakasama.reisedeutsch.BuildConfig.VERSION_CODE)}.onSuccess{release->_update.value=UpdateUi(release=release,error=if(release==null)"最新版を使用しています" else null)}.onFailure{_update.value=UpdateUi(error=it.message?:"更新を確認できませんでした")}}
    }
    fun downloadUpdate(context:Context){
        val release=_update.value.release?:return
        _update.value=_update.value.copy(downloading=true,error=null)
        viewModelScope.launch{runCatching{UpdateRepository.downloadAndPrepare(context,release)}.onSuccess{file->_update.value=_update.value.copy(downloading=false,apkPath=file.absolutePath);toast("更新ファイルを確認しました。OSの画面でインストールしてください")}.onFailure{_update.value=_update.value.copy(downloading=false,error=it.message?:"更新を準備できませんでした")}}
    }
    fun installUpdate(context:Context){
        val path=_update.value.apkPath?:return
        val file=java.io.File(path);if(!file.exists()){_update.value=_update.value.copy(error="更新ファイルがありません。ダウンロードし直してください");return}
        if(Build.VERSION.SDK_INT>=26&&!context.packageManager.canRequestPackageInstalls()){
            runCatching{context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))}.onFailure{_update.value=_update.value.copy(error="アプリのインストール設定を開けませんでした")};return
        }
        runCatching{context.startActivity(UpdateRepository.installerIntent(context,file))}.onFailure{_update.value=_update.value.copy(error="Androidのインストーラを開けませんでした: ${it.message}")}
    }
}
