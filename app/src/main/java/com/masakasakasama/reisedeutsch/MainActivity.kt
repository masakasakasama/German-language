package com.masakasakasama.reisedeutsch

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.masakasakasama.reisedeutsch.data.*
import com.masakasakasama.reisedeutsch.learning.Assessment
import com.masakasakasama.reisedeutsch.ui.*
import java.util.Locale
import kotlinx.coroutines.launch

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=android.graphics.Color.rgb(255,248,238);window.navigationBarColor=android.graphics.Color.rgb(255,248,238);window.decorView.systemUiVisibility=android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;setContent{ReiseApp()}}
}

private val navItems=listOf("home" to ("ホーム" to Icons.Default.Home),"lessons" to ("レッスン" to Icons.Default.Map),"conversation" to ("会話練習" to Icons.Default.Chat),"collection" to ("コレクション" to Icons.Default.CollectionsBookmark),"profile" to ("マイページ" to Icons.Default.Person))
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ReiseApp(vm:StudyViewModel= viewModel()){
 val page by vm.page.collectAsState();val stats by vm.stats.collectAsState();val session by vm.session.collectAsState();val dark by vm.dark.collectAsState();val notice by vm.notice.collectAsState();val goal by vm.goal.collectAsState();val update by vm.update.collectAsState()
 val courseUnit by vm.courseUnit.collectAsState()
 val context= LocalContext.current;val scope= rememberCoroutineScope();var drawer by remember{mutableStateOf(false)};var showUpdate by remember{mutableStateOf(false)};var showExit by remember{mutableStateOf(false)}
 BackHandler(enabled=!drawer&&!showUpdate&&!showExit){if(!vm.goBack())showExit=true}
 LaunchedEffect(update.release?.versionCode){if(update.release!=null)showUpdate=true}
 var tts by remember{mutableStateOf<TextToSpeech?>(null)}
 DisposableEffect(context){val engine=TextToSpeech(context){status->if(status==TextToSpeech.SUCCESS)tts?.language=Locale.GERMANY};tts=engine;onDispose{engine.stop();engine.shutdown()}}
 val speechLauncher= rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
   val spoken=result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
   if(spoken!=null){vm.setSpoken(spoken);vm.toast("聞き取り結果を確認してから送信してください")}else vm.toast("聞き取れませんでした。文字で練習できます")
 }
 val micPermission= rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->if(granted){val intent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);putExtra(RecognizerIntent.EXTRA_LANGUAGE,"de-DE");putExtra(RecognizerIntent.EXTRA_PROMPT,"ドイツ語で話してください");putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true)};speechLauncher.launch(intent)}else vm.toast("マイクを使わず文字で練習できます")}
 val exportLauncher= rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")){uri->if(uri!=null)scope.launch{runCatching{context.contentResolver.openOutputStream(uri)!!.bufferedWriter().use{it.write(vm.repository.exportJson())}}.onSuccess{vm.toast("バックアップを保存しました")}.onFailure{vm.toast("保存できませんでした")}}}
 val importLauncher= rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)scope.launch{runCatching{context.contentResolver.openInputStream(uri)!!.bufferedReader().use{it.readText()}.also{vm.repository.importJson(it)}}.onSuccess{vm.toast("バックアップを復元しました")}.onFailure{vm.toast("バックアップを読み込めませんでした: ${it.message}")}}}
 ReiseTheme(dark){
  val c=LocalReiseColors.current
  val snackbar= remember { SnackbarHostState() }
  LaunchedEffect(notice){notice?.let{snackbar.showSnackbar(it);vm.clearNotice()}}
  Scaffold(snackbarHost={SnackbarHost(snackbar)},containerColor=c.paper,topBar={
   if(page !in setOf("study","complete","conversation-detail"))TopAppBar(
    title={Text(if(page=="home")"ReiseDeutsch" else navItems.firstOrNull{it.first==page}?.second?.first?:when(page){"settings"->"設定";"backup"->"データ管理";"course"->"ドイツ語の基礎";"course-unit"->"しくみを学ぶ";"course-complete"->"基礎レッスン完了";else->"ReiseDeutsch"},fontWeight=FontWeight.Bold,color=c.ink)},
    navigationIcon={IconButton(onClick={if(page in setOf("settings","backup","course","course-unit","course-complete"))vm.goBack() else drawer=true}){Icon(if(page in setOf("settings","backup","course","course-unit","course-complete"))Icons.Default.ArrowBack else Icons.Default.Menu,null,tint=c.ink)}},
    actions={if(page=="home")IconButton(onClick=vm::dueClick){Icon(Icons.Default.Notifications,null,tint=c.ink)};if(page=="lessons")IconButton(onClick={vm.setPage("map")}){Icon(Icons.Default.Explore,null,tint=c.ink)}} ,colors=TopAppBarDefaults.topAppBarColors(containerColor=c.paper))
  },bottomBar={if(page in setOf("home","lessons","map","conversation","collection","profile"))NavigationBar(containerColor=c.surface,tonalElevation=2.dp){navItems.forEach{(id,pair)->NavigationBarItem(selected=page==id||id=="lessons"&&page=="map",onClick={vm.setPage(id)},icon={Icon(pair.second,null)},label={Text(pair.first,maxLines=1,fontSize=10.sp)},colors=NavigationBarItemDefaults.colors(selectedIconColor=c.action,selectedTextColor=c.action,indicatorColor=c.actionSoft,unselectedIconColor=c.muted,unselectedTextColor=c.muted))}}}){padding->
   Box(Modifier.fillMaxSize().padding(padding)){
    when(page){
     "home"->HomeScreen(vm,stats,goal,tts){vm.setPage("lessons")}
     "lessons","map"->LessonMapScreen(vm,stats)
     "course"->CourseOverviewScreen(vm,stats)
     "course-unit"->CourseUnitScreen(vm,courseUnit,tts)
     "course-complete"->CourseCompleteScreen(vm,courseUnit)
     "conversation"->ConversationList(vm)
     "conversation-detail"->ConversationScreen(vm,vm.conversation.collectAsState().value,tts,micPermission)
     "collection"->CollectionScreen(vm,stats)
     "profile"->ProfileScreen(vm,stats,exportLauncher,importLauncher,{vm.checkUpdate();showUpdate=true})
     "settings"->SettingsScreen(vm,goal)
     "backup"->BackupScreen({exportLauncher.launch("reisedeutsch-backup.json")},{importLauncher.launch(arrayOf("application/json","text/json"))},{scope.launch{vm.repository.reset();vm.toast("学習記録をリセットしました")}})
     "study"->session?.let{StudyScreen(vm,it,tts)}?:EmptyMessage("🐰","学習を選んでね","ホームからレッスンを選んでください")
     "complete"->CompleteScreen(vm,stats)
     else->HomeScreen(vm,stats,goal,tts){vm.setPage("lessons")}
    }
   }
  }
  if(showExit)AlertDialog(onDismissRequest={showExit=false},title={Text("アプリを閉じますか？")},text={Text("学習済みの記録は端末に保存されています。")},confirmButton={TextButton(onClick={showExit=false;(context as? android.app.Activity)?.finish()}){Text("閉じる")}},dismissButton={TextButton(onClick={showExit=false}){Text("学習を続ける")}})
  if(drawer)AlertDialog(onDismissRequest={drawer=false},title={Text("ReiseDeutsch 🐰")},text={Text("旅するように、ドイツ語を。\n短い練習を積み重ねて、旅のフレーズを覚えよう。")},confirmButton={TextButton(onClick={drawer=false;vm.setPage("settings")}){Text("設定")}},dismissButton={TextButton(onClick={drawer=false}){Text("閉じる")}})
  if(showUpdate)AlertDialog(onDismissRequest={showUpdate=false},title={Text("アプリの更新")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text(if(update.checking)"GitHub Releasesを確認しています…" else update.error?:update.release?.let{"ReiseDeutsch ${it.versionName}\n${it.notes}\n${it.sizeBytes/1024/1024} MB"}?:"更新を確認できませんでした");if(update.downloading)LinearProgressIndicator(Modifier.fillMaxWidth())}},confirmButton={TextButton(enabled=!update.checking&&!update.downloading,onClick={when{update.apkPath!=null->vm.installUpdate(context);update.release!=null->vm.downloadUpdate(context);else->vm.checkUpdate()}}){Text(when{update.apkPath!=null->"インストール";update.release!=null->if(update.downloading)"取得中…" else "ダウンロード";else->"もう一度確認"})}},dismissButton={TextButton(onClick={showUpdate=false}){Text("閉じる")}})
 }
}

@Composable private fun PageColumn(content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=16.dp, vertical=12.dp),verticalArrangement=Arrangement.spacedBy(14.dp),content=content)}
@Composable private fun HomeScreen(vm:StudyViewModel,s:AppStats,goal:Int,tts:TextToSpeech?,onLessons:()->Unit){
 val c=LocalReiseColors.current;val hour=java.time.LocalTime.now().hour;val greeting=when(hour){in 5..10->"Guten Morgen!";in 11..17->"Guten Tag!";else->"Guten Abend!"}
 PageColumn{
  ReiseCard(Modifier.fillMaxWidth().heightIn(min=130.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(greeting,fontSize=25.sp,fontWeight=FontWeight.ExtraBold,color=c.ink);Text("今日も一歩、ドイツ語で新しい世界に近づこう！",color=c.muted);TextButton(onClick={tts?.speak("Guten Morgen",TextToSpeech.QUEUE_FLUSH,null,"greeting")}){Text("🔊 こんにちはを聞く")}};TravelBunny(Modifier.size(82.dp))}}
  AnimatedTravelCompanion()
  CourseEntry(vm,s)
  ReiseCard(Modifier.fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically){Text("🎯",fontSize=34.sp);Column(Modifier.weight(1f).padding(horizontal=12.dp)){Text("今日のミッション",color=c.action,fontWeight=FontWeight.Bold);Text(if(s.today>=3)"今日の目標クリア！旅を続けよう 🐰" else "カフェで注文するフレーズを3つ練習しよう！",fontWeight=FontWeight.SemiBold);ProgressBar(s.today.coerceAtMost(3),3)};PrimaryButton("→",onClick={if(s.due>0)vm.dueClick() else onLessons()},Modifier.width(64.dp))}}
  ReiseCard(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("今日の目標",fontWeight=FontWeight.Bold);Text("${s.today.coerceAtMost(goal)} / $goal 枚",color=c.muted)};ProgressBar(s.today,goal)}
  Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Metric("🔥","${s.streak}日","連続学習",Modifier.weight(1f));Metric("👑","${s.xp}","合計XP",Modifier.weight(1f));Metric("📊","A1","学習の目安",Modifier.weight(1f))}
  Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween,modifier=Modifier.fillMaxWidth()){Text("レッスンをえらぶ",fontSize=20.sp,fontWeight=FontWeight.Bold);TextButton(onClick=onLessons){Text("すべて見る →",color=c.action)}}
  val scenes=listOf("cafe" to "カフェ","station" to "駅・電車","hotel" to "ホテル","sightseeing" to "観光・街歩き")
  scenes.chunked(2).forEach{row->Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){row.forEach{(id,name)->val lesson=vm.content.lessons.first{it.scene==id};SceneCard(id,name,"${vm.content.lessons.count{it.scene==id&&it.id in s.completedLessons} }/5",Modifier.weight(1f),onClick={vm.startLesson(lesson)})}}}
  if(s.due>0)PrimaryButton("復習する ${s.due}枚 →",vm::dueClick)
 }
}
@Composable private fun Metric(icon:String,value:String,label:String,modifier:Modifier=Modifier){val c=LocalReiseColors.current;ReiseCard(modifier){Text(icon,fontSize=20.sp);Text(value,fontWeight=FontWeight.ExtraBold,fontSize=21.sp,color=c.ink);Text(label,fontSize=11.sp,color=c.muted,maxLines=1)}}
@Composable private fun SceneCard(scene:String,name:String,progress:String,modifier:Modifier=Modifier,onClick:()->Unit){val c=LocalReiseColors.current;ReiseCard(modifier.clickable{onClick()}){Text(sceneIcon(scene),fontSize=36.sp);Text(name,fontWeight=FontWeight.Bold);Text(sceneTitle(scene),fontSize=12.sp,color=c.muted);ProgressBar(progress.substringBefore('/').toIntOrNull()?:0,5)}}
private fun sceneTitle(scene:String)=when(scene){"cafe"->"Café";"station"->"Am Bahnhof";"hotel"->"Im Hotel";else->"Sightseeing"}

@Composable private fun LessonMapScreen(vm:StudyViewModel,s:AppStats){
 val c=LocalReiseColors.current;var listMode by remember{mutableStateOf(false)}
 PageColumn{
  ReiseCard(Modifier.fillMaxWidth()){Row(horizontalArrangement=Arrangement.SpaceBetween,modifier=Modifier.fillMaxWidth()){MetricInline("🔥","${s.streak}日","連続中");MetricInline("👑","${s.xp} XP","旅の記録")};Spacer(Modifier.height(4.dp));Row(horizontalArrangement=Arrangement.SpaceBetween,modifier=Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("ドイツを旅する学習マップ",fontWeight=FontWeight.Bold);TextButton(onClick={listMode=!listMode}){Text(if(listMode)"地図へ" else "一覧へ",color=c.action)}}}
  CourseEntry(vm,s)
  if(!listMode)MapCard(vm,s)
  vm.content.cities.forEach{city->val ls=city.lessonIds.mapNotNull(vm.content::lesson);val done=ls.count{it.id in s.completedLessons};val opened=city.order==1||vm.content.cities.getOrNull(city.order-2)?.lessonIds?.all{s.completedLessons.contains(it)}==true
   ReiseCard(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(if(done==4)"✅" else if(opened)"📍" else "🔒",fontSize=24.sp);Column(Modifier.weight(1f).padding(start=12.dp)){Text(city.name,fontWeight=FontWeight.Bold,fontSize=19.sp);Text("${done}/4 レッスン",color=c.muted)};Text(sceneIcon(ls.firstOrNull()?.scene?:""),fontSize=23.sp)};ProgressBar(done,4)
    ls.forEach{lesson->val available=opened||lesson.id in s.completedLessons;Row(Modifier.fillMaxWidth().clickable(enabled=available){vm.startLesson(lesson)}.padding(vertical=7.dp),verticalAlignment=Alignment.CenterVertically){Text(if(lesson.id in s.completedLessons)"✅" else if(available)"🔴" else "⚪",fontSize=16.sp);Text(lesson.title,Modifier.weight(1f).padding(start=10.dp),fontWeight=FontWeight.SemiBold);Text(if(available)"はじめる ›" else "次の街",color=if(available)c.action else c.muted,fontSize=12.sp)}}}
  }
 }
}
@Composable private fun MetricInline(icon:String,value:String,label:String){val c=LocalReiseColors.current;Column(horizontalAlignment=Alignment.CenterHorizontally){Text("$icon $value",fontWeight=FontWeight.Bold,fontSize=18.sp,color=c.ink);Text(label,fontSize=11.sp,color=c.muted)}}
@Composable private fun MapCard(vm:StudyViewModel,s:AppStats){val c=LocalReiseColors.current;val names=vm.content.cities.map{it.name};val travelDone=vm.content.cities.sumOf{city->city.lessonIds.count{it in s.completedLessons}};val nodes=listOf(0.24f to 0.08f,0.72f to 0.24f,0.19f to 0.43f,0.38f to 0.63f,0.66f to 0.84f)
 Box(Modifier.fillMaxWidth().height(460.dp).clip(RoundedCornerShape(26.dp)).background(Brush.verticalGradient(listOf(Color(0xFFEDF5D7),c.mapPaper,Color(0xFFE4F3E9))))){Canvas(Modifier.fillMaxSize()){val pts=nodes.map{Offset(size.width*it.first,size.height*it.second)};for(i in 0 until pts.lastIndex){drawLine(if(i<travelDone/4)c.progress else c.action,pts[i],pts[i+1],strokeWidth=6.dp.toPx(),pathEffect=PathEffect.dashPathEffect(floatArrayOf(12f,10f)))};pts.forEachIndexed{i,p->drawCircle(if(i<=travelDone/4)c.action else Color.White,14.dp.toPx(),p);drawCircle(if(i<=travelDone/4)c.action else c.muted,22.dp.toPx(),p,style=Stroke(3.dp.toPx()))}}
  Box(Modifier.align(Alignment.TopEnd).padding(10.dp)){Text("🌲     🏰      🌳\n    🌲        🏰",fontSize=32.sp,lineHeight=52.sp,color=Color(0xFF60775A))}
  nodes.forEachIndexed{i,pos->Column(Modifier.align(Alignment.TopStart).offset(x=(pos.first*310+8).dp,y=(pos.second*420+5).dp).widthIn(max=150.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(if(i<travelDone/4)"✅" else if(i==travelDone/4)"🐰" else "",fontSize=23.sp);Text(names[i],fontWeight=FontWeight.Bold,fontSize=13.sp,color=c.ink);Text("${vm.content.cities[i].lessonIds.count{s.completedLessons.contains(it)}}/4",fontSize=11.sp,color=c.muted)}}
  Text("DEUTSCHLAND 🇩🇪",Modifier.align(Alignment.BottomEnd).padding(14.dp),color=c.muted,fontSize=12.sp)
 }
}

@Composable private fun ConversationList(vm:StudyViewModel){val c=LocalReiseColors.current;PageColumn{BunnyCard("Hallo! どこで話してみる？","場面を選んで、ドイツ語で会話してみよう。🐰");val groups=vm.content.conversations.groupBy{it.scene};listOf("cafe","station","hotel","sightseeing").forEach{scene->Text("${sceneIcon(scene)} ${sceneTitle(scene)}",fontSize=20.sp,fontWeight=FontWeight.Bold);groups[scene].orEmpty().forEach{con->ReiseCard(Modifier.fillMaxWidth().clickable{vm.startConversation(con)}){Row(verticalAlignment=Alignment.CenterVertically){Text(sceneIcon(scene),fontSize=30.sp);Column(Modifier.weight(1f).padding(start=12.dp)){Text(con.title,fontWeight=FontWeight.Bold);Text(con.turns.first().german,color=c.muted)};Text("›",color=c.action,fontSize=24.sp)}}}}}}

@Composable private fun ConversationScreen(vm:StudyViewModel,con:Conversation?,tts:TextToSpeech?,permission:androidx.activity.result.ActivityResultLauncher<String>){
 if(con==null){EmptyMessage("🐰","会話を選んでね","場面一覧からはじめよう");return};val c=LocalReiseColors.current;val context=LocalContext.current;val transcript by vm.spoken.collectAsState();var typed by remember(con.id){mutableStateOf("")};var showJapanese by remember{mutableStateOf(true)};var turn by remember(con.id){mutableIntStateOf(0)};LaunchedEffect(transcript){if(transcript.isNotBlank()){typed=transcript;vm.clearSpoken()}}
 PageColumn{BunnyCard("${sceneIcon(con.scene)} ${con.title}","ゆっくり聞いて、声に出してみよう。答えは画面に表示されるよ。");Row(horizontalArrangement=Arrangement.SpaceBetween,modifier=Modifier.fillMaxWidth()){Text("会話の練習",fontWeight=FontWeight.Bold);Text("${turn+1}/${con.turns.size}",color=c.muted)};ProgressBar(turn+1,con.turns.size)
  con.turns.take(if(turn>=2)con.turns.size else turn+1).forEachIndexed{i,t->val learner=t.speaker=="LEARNER";Row(Modifier.fillMaxWidth(),horizontalArrangement=if(learner)Arrangement.End else Arrangement.Start){Surface(color=if(learner)c.speech else c.surface,contentColor=if(learner)c.onSpeech else c.ink,shape=RoundedCornerShape(22.dp),border=if(learner)null else androidx.compose.foundation.BorderStroke(1.dp,c.line),modifier=Modifier.fillMaxWidth(0.88f)){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Text(t.german,fontSize=19.sp,fontWeight=FontWeight.SemiBold);if(showJapanese)Text(t.japanese,fontSize=14.sp,color=if(learner)c.onSpeech.copy(alpha=.83f) else c.muted);TextButton(onClick={tts?.speak(t.german,TextToSpeech.QUEUE_FLUSH,null,"conv-$i")}){Text("🔊 ドイツ語を聞く",color=if(learner)c.onSpeech else c.speech)}}}}}
  if(turn<2){PrimaryButton("🎙️ タップして話す",{if(!SpeechRecognizer.isRecognitionAvailable(context)){vm.toast("この端末では音声認識を使えません。下の入力欄をご利用ください")}else permission.launch(android.Manifest.permission.RECORD_AUDIO)},Modifier.fillMaxWidth());OutlinedTextField(typed,{typed=it},Modifier.fillMaxWidth(),label={Text("ドイツ語で答える")},placeholder={Text("Ä Ö Ü ß も入力できます")},minLines=1,maxLines=3);val learnerTurn=con.turns.firstOrNull{it.speaker=="LEARNER"};PrimaryButton("答えを送信 →",{learnerTurn?.cardId?.let{vm.saveConversationAnswer(con.id,it,typed)};val card=learnerTurn?.cardId?.let(vm.content::card);val correct=card?.let{vm.grade(it,typed)}==true;vm.toast(if(correct)"Gut gemacht! 会話のフレーズを覚えたよ 🎉" else "答えを記録しました。会話の続きも見てみよう 🐰");turn=2},enabled=typed.isNotBlank())}else{BunnyCard("Sehr gut!","会話の練習ができたよ 🎉");PrimaryButton("会話一覧へ",{vm.setPage("conversation")})}
  Row(verticalAlignment=Alignment.CenterVertically){Text("日本語訳を表示",modifier=Modifier.weight(1f));Switch(showJapanese,{showJapanese=it})}
 }
}

@Composable private fun CollectionScreen(vm:StudyViewModel,s:AppStats){val c=LocalReiseColors.current;var query by remember{mutableStateOf("")};val f by vm.filter.collectAsState();val cards=vm.content.cards.filter{it.kind=="PHRASE"}.filter{it.id in s.collected}.filter{f=="ALL"||s.assessments[it.id]==f}.filter{query.isBlank()||it.german.contains(query,true)||it.japanese.contains(query)}
 PageColumn{Text("フレーズカード",fontSize=22.sp,fontWeight=FontWeight.Bold);Text("${s.collected.size} / ${vm.content.cards.count{it.kind=="PHRASE"}} 集めたフレーズ",color=c.muted);OutlinedTextField(query,{query=it},modifier=Modifier.fillMaxWidth(),label={Text("フレーズを検索")},singleLine=true);Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){listOf("ALL" to "すべて","KNOW" to "わかる","OK" to "少し","DONT_KNOW" to "復習").forEach{(id,label)->FilterChip(selected=f==id,onClick={vm.setFilter(id)},label={Text(label)},colors=FilterChipDefaults.filterChipColors(selectedContainerColor=c.actionSoft))}};if(cards.isEmpty())EmptyMessage("🎒","まだカードがありません","レッスンで新しいフレーズを学ぼう") else cards.forEach{card->PhraseTile(card,{vm.startCard(card)},s.assessments[card.id])}}
}

@Composable private fun ProfileScreen(vm:StudyViewModel,s:AppStats,export:androidx.activity.result.ActivityResultLauncher<String>,import:androidx.activity.result.ActivityResultLauncher<Array<String>>,updates:()->Unit){val c=LocalReiseColors.current;PageColumn{BunnyCard("Meine Reise 🐰","ドイツ語の旅の記録。できたことを見てみよう！");ReiseCard(Modifier.fillMaxWidth()){Text("今日の記録",fontWeight=FontWeight.Bold,fontSize=18.sp);Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){Metric("📚","${s.today}","カード");Metric("⭐","${s.xp}","合計XP");Metric("🔥","${s.streak}","連続日数")}};ReiseCard(Modifier.fillMaxWidth()){Text("学習の進み具合",fontWeight=FontWeight.Bold);Text("${s.learned} 枚を学習・${s.mastered} 枚が定着目安",color=c.muted);ProgressBar(s.collected.size,vm.content.cards.count{it.kind=="PHRASE"});Text("現在の教材目安: A1",color=c.muted)};ProfileRow("⚙️","設定","表示・目標・通知",{vm.setPage("settings")});ProfileRow("📦","バックアップ","学習記録を保存・復元",{vm.setPage("backup")});ProfileRow("⬆️","アプリを更新","バージョン ${BuildConfig.VERSION_NAME}・確認して更新",updates)} }
@Composable private fun ProfileRow(icon:String,title:String,subtitle:String,onClick:()->Unit){val c=LocalReiseColors.current;ReiseCard(Modifier.fillMaxWidth().clickable{onClick()}){Row(verticalAlignment=Alignment.CenterVertically){Text(icon,fontSize=26.sp);Column(Modifier.weight(1f).padding(horizontal=12.dp)){Text(title,fontWeight=FontWeight.Bold);Text(subtitle,color=c.muted,fontSize=12.sp)};Text("›",color=c.action,fontSize=24.sp)}}}
@Composable private fun SettingsScreen(vm:StudyViewModel,goal:Int){val c=LocalReiseColors.current;val dark by vm.dark.collectAsState();val autoUpdates by vm.autoUpdates.collectAsState();PageColumn{BunnyCard("Einstellungen","自分のペースに合わせよう 🌼");ReiseCard(Modifier.fillMaxWidth()){Text("テーマ",fontWeight=FontWeight.Bold);Row{FilterChip(!dark,{vm.setDark(false)},{Text("☀️ Light")});Spacer(Modifier.width(8.dp));FilterChip(dark,{vm.setDark(true)},{Text("🌙 Dark")})}};ReiseCard(Modifier.fillMaxWidth()){Text("1日の目標",fontWeight=FontWeight.Bold);Row{listOf(5,10,20).forEach{FilterChip(goal==it,{vm.setGoal(it)},{Text("$it 枚")})}}};ReiseCard(Modifier.fillMaxWidth()){Text("更新のお知らせ",fontWeight=FontWeight.Bold);Row(verticalAlignment=Alignment.CenterVertically){Text("起動時に新しい版を確認",Modifier.weight(1f));Switch(autoUpdates,{vm.setAutoUpdates(it)})};Text("新しい版があると案内します。ダウンロードとインストールは自分で確認して進めます。",color=c.muted)};ReiseCard(Modifier.fillMaxWidth()){Text("学習記録と教材はこの端末に保存されます。オンライン同期や広告はありません。",color=c.muted)}}}
@Composable private fun BackupScreen(onExport:()->Unit,onImport:()->Unit,onReset:()->Unit){val c=LocalReiseColors.current;var confirm by remember{mutableStateOf(false)};PageColumn{BunnyCard("データを守ろう 🧳","バックアップはJSON形式で保存されます。復元すると現在の学習記録が置き換わります。");PrimaryButton("⬇️ バックアップを書き出す",onExport);PrimaryButton("⬆️ バックアップから復元",onImport);ReiseCard(Modifier.fillMaxWidth().clickable{confirm=true}){Text("学習記録をリセット",color=c.action,fontWeight=FontWeight.Bold);Text("教材は消えません",color=c.muted)}};if(confirm)AlertDialog(onDismissRequest={confirm=false},title={Text("学習記録を消去しますか？")},text={Text("先にバックアップを保存してください。消去した記録は元に戻せません。")},confirmButton={TextButton(onClick={confirm=false;onReset()}){Text("リセット",color=c.action)}},dismissButton={TextButton(onClick={confirm=false}){Text("キャンセル")}})}

@Composable private fun StudyScreen(vm:StudyViewModel,s:SessionUi,tts:TextToSpeech?){
 val c=LocalReiseColors.current
 val inputMode=s.inputMode
 val typed=s.typed
 val card=s.cards.getOrNull(s.cursor/2)?:return
 val isIntro=s.cursor%2==0
 val question=GermanCourse.question(card.id)
 val course=GermanCourse.unit(s.lesson?.id.orEmpty())
 val options=remember(s.id,s.cursor){vm.options(card,(s.id+":"+s.cursor).hashCode())}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
   IconButton(onClick={vm.goBack()}){Icon(Icons.Default.Close,"閉じる",tint=c.ink)}
   Column(Modifier.weight(1f)){Text(s.lesson?.title?:"今日の復習",fontWeight=FontWeight.Bold);ProgressBar(s.cursor,s.cards.size*2)}
   Text("${s.cursor+1}/${s.cards.size*2}",color=c.muted)
  }
  if(course!=null)TextButton(onClick={vm.openCourseUnit(course)}){Text("📖 しくみの説明に戻る")}
  BunnyCard(if(isIntro)"文の形と理由を見てみよう" else "しくみを使って考えよう 💭",if(question!=null)"主語・動詞・格に注目。答えだけでなく、なぜその形になるか考えてみよう。" else "ゆっくりで大丈夫。旅の準備をしよう。")
  if(isIntro){
   ReiseCard(Modifier.fillMaxWidth()){
    Text(if(question!=null)"🌱" else sceneIcon(card.scene),fontSize=36.sp)
    Text(card.german,fontSize=25.sp,fontWeight=FontWeight.Bold,color=c.ink)
    TextButton(onClick={tts?.speak(card.german,TextToSpeech.QUEUE_FLUSH,null,card.id)}){Text("🔊 発音を聞く")}
    if(s.revealed){Text(card.japanese,fontSize=19.sp,color=c.muted);Text("なぜこの形？",fontWeight=FontWeight.Bold);Text(card.grammar,fontSize=16.sp,lineHeight=26.sp)}
   }
   if(!s.revealed)PrimaryButton("意味と理由を見る",vm::reveal,Modifier.fillMaxWidth()) else {
    Text("どれくらいわかった？",color=c.muted)
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
     Assessment.values().forEach{a->val title=when(a){Assessment.DONT_KNOW->"😕\nわからない";Assessment.OK->"😐\n少しわかる";Assessment.KNOW->"😊\nわかる"}
      OutlinedButton(onClick={vm.saveAssessment(a)},modifier=Modifier.weight(1f).heightIn(min=76.dp),enabled=!s.answered&&!s.saving,shape=RoundedCornerShape(18.dp)){Text(title,textAlign=TextAlign.Center,fontSize=12.sp)}
     }
    }
    if(s.answered)PrimaryButton("練習へ →",vm::next,Modifier.fillMaxWidth())
   }
  }else{
   ReiseCard(Modifier.fillMaxWidth()){
    Text(question?.prompt?:"${card.japanese} をドイツ語で選ぼう",fontWeight=FontWeight.Bold,fontSize=20.sp)
    if(inputMode&&question!=null)Text("意味：${card.japanese}\n文全体を入力してください。大文字・小文字を区別します。",color=c.muted)
    if(question==null)TextButton(onClick={tts?.speak(card.german,TextToSpeech.QUEUE_FLUSH,null,card.id)}){Text("🔊 ドイツ語を聞く")}
   }
   if(!inputMode)options.forEachIndexed{i,opt->
    val selected=s.selected==i
    OutlinedButton(onClick={vm.choose(i)},enabled=!s.answered&&!s.saving,modifier=Modifier.fillMaxWidth().heightIn(min=54.dp),shape=RoundedCornerShape(16.dp),border=androidx.compose.foundation.BorderStroke(if(selected)2.dp else 1.dp,if(selected)c.action else c.line),colors=ButtonDefaults.outlinedButtonColors(containerColor=if(selected)c.actionSoft else c.surface,contentColor=c.ink)){Text(opt.german,fontSize=17.sp)}
   }else{
    OutlinedTextField(typed,vm::typeAnswer,Modifier.fillMaxWidth(),enabled=!s.answered&&!s.saving,label={Text("ドイツ語の文を入力")},minLines=1,maxLines=3)
    Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("ä","ö","ü","ß").forEach{letter->AssistChip(enabled=!s.answered&&!s.saving,onClick={vm.typeAnswer(typed+letter)},label={Text(letter)})}}
   }
   TextButton(enabled=!s.answered&&!s.saving,onClick=vm::toggleInputMode){Text(if(inputMode)"選択問題で答える" else "文字で入力する")}
   if(s.answered){
    val correct=s.answerCorrect==true
    ReiseCard(Modifier.fillMaxWidth()){
     Text(if(correct)"Sehr gut! 正解 🎉" else "理由を読んで、もう一度 🐰",fontWeight=FontWeight.Bold,color=if(correct)c.progress else c.action)
     Text(card.german,fontWeight=FontWeight.Bold,fontSize=20.sp)
     Text(card.japanese,color=c.muted)
     Text("なぜこの答え？\n${card.grammar}",lineHeight=26.sp)
    }
    if(course!=null&&!correct)PrimaryButton("もう一度答える",vm::retryQuestion,Modifier.fillMaxWidth())
    else PrimaryButton("次へ →",vm::next,Modifier.fillMaxWidth())
   }else PrimaryButton(if(s.saving)"保存中…" else "答えを確認",{vm.saveAnswer(if(inputMode)vm.grade(card,typed) else options.getOrNull(s.selected?:-1)?.id==card.id)},Modifier.fillMaxWidth(),enabled=!s.saving&&(if(inputMode)typed.isNotBlank() else s.selected!=null))
  }
 }
}

@Composable private fun CompleteScreen(vm:StudyViewModel,s:AppStats){val c=LocalReiseColors.current;Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text("🐰🎒🎉",fontSize=74.sp);Text("Gut gemacht!",fontSize=30.sp,fontWeight=FontWeight.ExtraBold,color=c.action);Text("レッスンを完了しました",fontSize=21.sp,fontWeight=FontWeight.Bold);Text("フレーズを学んで、ドイツの旅を一歩進めたよ。",color=c.muted,textAlign=TextAlign.Center);Spacer(Modifier.height(20.dp));PrimaryButton("ホームへ戻る",{vm.setPage("home")})}}
