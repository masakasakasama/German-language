package com.masakasakasama.reisedeutsch.ui

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masakasakasama.reisedeutsch.AppStats
import com.masakasakasama.reisedeutsch.StudyViewModel
import com.masakasakasama.reisedeutsch.data.GermanCourse
import com.masakasakasama.reisedeutsch.data.GrammarUnit

@Composable private fun CoursePage(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
}

@Composable fun CourseEntry(vm: StudyViewModel, stats: AppStats) {
    val done = GermanCourse.units.count { it.id in stats.completedLessons }
    ReiseCard(Modifier.fillMaxWidth().clickable { vm.setPage("course") }) {
        Text("🌱 ゼロからわかるドイツ語", fontWeight = FontWeight.Bold, fontSize = 21.sp)
        Text("丸暗記の前に、文を作るしくみを知ろう。14課・42問・理由つき解説。", color = LocalReiseColors.current.muted)
        ProgressBar(done, GermanCourse.units.size)
        PrimaryButton(if (done == 0) "基礎コースをはじめる →" else "基礎コースを続ける →", { vm.setPage("course") })
    }
}

@Composable fun CourseOverviewScreen(vm: StudyViewModel, stats: AppStats) {
    val next = GermanCourse.units.firstOrNull { it.id !in stats.completedLessons }
    CoursePage {
        BunnyCard("文のしくみから、ドイツ語へ 🌱", "目安は1課10〜15分。説明 → 文の分解 → 3枚の理解チェックと3問の練習 → 間隔を空けた復習。何度でも読み返せます。")
        ProgressBar(GermanCourse.units.count { it.id in stats.completedLessons }, GermanCourse.units.size)
        next?.let { PrimaryButton("次の課：${it.title}", { vm.openCourseUnit(it) }, Modifier.fillMaxWidth()) }
        Text("おすすめは上から順番。必要な課へ戻っても大丈夫。", color = LocalReiseColors.current.muted)
        GermanCourse.units.forEach { unit ->
            ReiseCard(Modifier.fillMaxWidth().clickable { vm.openCourseUnit(unit) }) {
                Text("${if (unit.id in stats.completedLessons) "✅" else "📖"} ${unit.title}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(unit.goal, color = LocalReiseColors.current.muted)
                TextButton(onClick = { vm.openCourseUnit(unit) }) { Text("説明を読む →") }
            }
        }
        ReiseCard(Modifier.fillMaxWidth()) {
            Text("このコースの到達点", fontWeight = FontWeight.Bold)
            Text("初級の文を『主語・動詞・格・文の枠』に分けて考えられる土台を作ります。ドイツ語の全ルールやA1試験範囲すべてを網羅するコースではありません。旅行レッスンと組み合わせて、読んだルールを実際の文で使いましょう。")
        }
    }
}

@Composable fun CourseUnitScreen(vm: StudyViewModel, unit: GrammarUnit, tts: TextToSpeech?) {
    CoursePage {
        BunnyCard(unit.title, "今日できるようになること：${unit.goal}")
        unit.sections.forEach { section ->
            ReiseCard(Modifier.fillMaxWidth()) {
                Text(section.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(section.body, fontSize = 16.sp, lineHeight = 27.sp)
            }
        }
        Text("🔎 例文を分解してみよう", fontSize = 21.sp, fontWeight = FontWeight.Bold)
        unit.examples.forEachIndexed { index, example ->
            ReiseCard(Modifier.fillMaxWidth()) {
                Text(example.german, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(example.japanese, color = LocalReiseColors.current.muted)
                Text(example.parts, fontWeight = FontWeight.SemiBold, lineHeight = 25.sp)
                Text("なぜこの形？\n${example.reason}", lineHeight = 25.sp)
                TextButton(onClick = { tts?.speak(example.german, TextToSpeech.QUEUE_FLUSH, null, "${unit.id}-example-$index") }) {
                    Text("🔊 例文の音を聞く")
                }
            }
        }
        BunnyCard("考える順番 💡", unit.tip)
        Text("練習では、答えを見る → わかる度合いを選ぶ → 理由を考えて答える。間違えたら解説を読んで再挑戦します。名詞の大文字も確認するため、基礎コースの入力問題では大文字・小文字を区別します。", color = LocalReiseColors.current.muted)
        PrimaryButton("理解チェックと練習へ →", { vm.startLesson(unit.practiceLesson()) }, Modifier.fillMaxWidth())
        TextButton(onClick = { vm.setPage("course") }) { Text("コース一覧へ戻る") }
    }
}

@Composable fun CourseCompleteScreen(vm: StudyViewModel, unit: GrammarUnit) {
    val next = GermanCourse.units.getOrNull(GermanCourse.units.indexOf(unit) + 1)
    CoursePage {
        BunnyCard("Gut gemacht! 🎉", "${unit.title} の練習を完了しました。正解できたことと、時間を空けても思い出せることは別。カードはホームの復習にも出ます。")
        ReiseCard(Modifier.fillMaxWidth()) {
            Text("今日のポイント", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(unit.tip)
        }
        next?.let { PrimaryButton("次の課へ：${it.title}", { vm.openCourseUnit(it) }, Modifier.fillMaxWidth()) }
        PrimaryButton("コース一覧へ", { vm.setPage("course") }, Modifier.fillMaxWidth())
        TextButton(onClick = { vm.openCourseUnit(unit) }) { Text("もう一度説明を読む") }
    }
}
