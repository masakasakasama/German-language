package com.masakasakasama.reisedeutsch.data

data class GrammarSection(val title: String, val body: String)
data class GrammarExample(val german: String, val japanese: String, val parts: String, val reason: String)
data class GrammarQuestion(val prompt: String, val answer: String, val japanese: String, val explanation: String, val choices: List<String>)
data class GrammarUnit(
    val id: String, val title: String, val goal: String,
    val sections: List<GrammarSection>, val examples: List<GrammarExample>,
    val questions: List<GrammarQuestion>, val tip: String,
) {
    fun cardId(index: Int) = "$id-q${index + 1}"
    fun cards() = questions.mapIndexed { index, q ->
        PhraseCard(cardId(index), q.answer, q.japanese, "foundation", "PHRASE", grammar = q.explanation)
    }
    fun practiceLesson() = Lesson(id, "", "foundation", title, questions.indices.map(::cardId), goal)
}

/** Each unit builds on the previous one. Its practice cards use the existing SRS and backup. */
object GermanCourse {
    private fun section(title: String, body: String) = GrammarSection(title, body)
    private fun example(de: String, ja: String, parts: String, reason: String) = GrammarExample(de, ja, parts, reason)
    private fun q(prompt: String, answer: String, ja: String, why: String, vararg wrong: String) =
        GrammarQuestion(prompt, answer, ja, why, listOf(answer) + wrong)

    val units = listOf(
        GrammarUnit("foundation-01", "01 音と文字の地図", "文字と音の対応、名詞の大文字を知る", listOf(
            section("まず音の組み合わせを読む", "ei は『アイ』、ie は長い『イー』。Reise（旅）と Riese（巨人）は文字の順番で音も意味も変わります。w は英語の v に近い音、z は『ツ』。カタカナは目安なので、音声も聞きましょう。"),
            section("ä・ö・ü・ß と大文字", "ä・ö・ü は飾りではなく別の母音です。ö・ü は唇を丸めたまま、それぞれ『エ』『イ』に近い舌の位置で発音します。ß は s の音。ドイツ語では名詞を文の途中でも大文字で始めます：Hotel、Kaffee、Reise。文頭も大文字です。"),
        ), listOf(
            example("Die Reise ist schön.", "旅はすてきです。", "Die Reise＝旅 ｜ ist＝です ｜ schön＝すてき", "Reise の ei は『アイ』。名詞 Reise は大文字です。"),
            example("Das ist ein Hotel.", "これはホテルです。", "Das＝これ ｜ ist＝です ｜ ein Hotel＝あるホテル", "Hotel は名詞なので、文の途中でも H を大文字にします。"),
        ), listOf(
            q("『旅』を意味する Reise を使う文を選ぼう。", "Die Reise ist schön.", "旅はすてきです。", "Reise は『旅』、Riese は『巨人』。ei と ie の順番を見ます。", "Der Riese ist schön.", "Der Reis ist schön."),
            q("名詞の大文字が正しい文は？", "Das ist ein Hotel.", "これはホテルです。", "Hotel は名詞なので大文字。ein は冠詞なので、文中では小文字です。", "Das ist ein hotel.", "Das ist Ein Hotel."),
            q("『コーヒー』を正しく書いた文は？", "Der Kaffee ist gut.", "そのコーヒーはおいしいです。", "Kaffee は名詞、gut は形容詞です。名詞だけを大文字で始めます。", "Der kaffee ist gut.", "Der Kaffee ist Gut."),
        ), "綴りを丸暗記する前に、文字のまとまりと名詞を探しましょう。"),
        GrammarUnit("foundation-02", "02 文の芯：だれが・どうする", "平叙文の動詞第2位を理解する", listOf(
            section("日本語は最後、ドイツ語は第2位", "日本語の『私は今日コーヒーを飲む』では動詞が最後。ドイツ語の普通の文では、人称に合わせた動詞が第2位です。Ich trinke heute Kaffee.＝私は／飲む／今日／コーヒー。まず『だれが』『どうする』の芯を作ります。"),
            section("第2位は2語目ではなく2つ目のかたまり", "Heute trinke ich Kaffee. のように『今日』を先頭にもできます。その場合、動詞 trinke は第2位を保ち、主語 ich は後ろへ。Am Morgen（朝に）のような複数語も、ひとつのかたまりとして数えます。"),
        ), listOf(
            example("Ich trinke heute Kaffee.", "私は今日コーヒーを飲みます。", "Ich＝主語 ｜ trinke＝動詞 ｜ heute＝時 ｜ Kaffee＝飲むもの", "主語から始めた平叙文。動詞は第2位です。"),
            example("Am Morgen trinke ich Kaffee.", "朝、私はコーヒーを飲みます。", "Am Morgen＝第1のかたまり ｜ trinke＝第2 ｜ ich＝主語 ｜ Kaffee", "Am と Morgen を別々の位置として数えません。"),
        ), listOf(
            q("『私は今日コーヒーを飲む』。主語から始める文は？", "Ich trinke heute Kaffee.", "私は今日コーヒーを飲みます。", "Ich の次に活用した動詞 trinke を置きます。日本語のように文末には置きません。", "Ich heute Kaffee trinke.", "Ich heute trinke Kaffee."),
            q("『今日』から始めても動詞を第2位に置くには？", "Heute trinke ich Kaffee.", "今日、私はコーヒーを飲みます。", "Heute → trinke → ich。時間を先頭にしたので、主語は動詞の後ろです。", "Heute ich trinke Kaffee.", "Heute ich Kaffee trinke."),
            q("Am Morgen を先頭に置いた正しい文は？", "Am Morgen trinke ich Kaffee.", "朝、私はコーヒーを飲みます。", "Am Morgen がひとつのかたまり。第2のかたまりが動詞 trinke です。", "Am trinke Morgen ich Kaffee.", "Am Morgen ich trinke Kaffee."),
        ), "平叙文を見たら、まず活用した動詞を囲んで第2位か確認。"),
        GrammarUnit("foundation-03", "03 主語で変わる動詞", "人称と規則動詞の現在形を作る", listOf(
            section("語幹＋語尾で『だれが』を示す", "lernen（学ぶ）から -en を取ると語幹 lern-。ich（私）なら -e、du（君）なら -st、er/sie/es（彼・彼女・それ）なら -t：ich lerne、du lernst、er lernt。"),
            section("複数と丁寧な Sie", "wir（私たち）-en、ihr（君たち）-t、sie（彼ら）-en、Sie（丁寧なあなた・あなた方）-en。丁寧な Sie はいつも大文字です。語幹が -t/-d で終わる arbeiten などでは du arbeitest のように e が入ります。これは基本の規則で、不規則動詞もあります。"),
        ), listOf(
            example("Du lernst Deutsch.", "君はドイツ語を学んでいます。", "Du＝君 ｜ lern-st＝語幹＋君の語尾 ｜ Deutsch＝ドイツ語", "主語 du なので語尾 -st。"),
            example("Wir lernen Deutsch.", "私たちはドイツ語を学んでいます。", "Wir＝私たち ｜ lern-en ｜ Deutsch", "主語 wir では -en になり、不定詞 lernen と同じ形です。"),
        ), listOf(
            q("主語 Ich に合う lernen の形は？", "Ich lerne Deutsch.", "私はドイツ語を学んでいます。", "ich には -e。lern- + e = lerne。", "Ich lernst Deutsch.", "Ich lernt Deutsch."),
            q("主語 Du に合う形は？", "Du lernst Deutsch.", "君はドイツ語を学んでいます。", "du には -st。lern- + st = lernst。", "Du lernen Deutsch.", "Du lerne Deutsch."),
            q("丁寧な『あなた』に合う形は？", "Sie lernen Deutsch.", "あなたはドイツ語を学んでいます。", "丁寧な Sie は大文字で、動詞の語尾は -en。er/sie/es の -t と区別します。", "Sie lernst Deutsch.", "Sie lerne Deutsch."),
        ), "動詞の形を選ぶときは、先に主語を決めます。"),
        GrammarUnit("foundation-04", "04 sein と haben", "『である・いる』と『持っている』の基本形", listOf(
            section("sein は状態・身分・場所", "sein は英語の be に近い動詞。ich bin、du bist、er/sie/es ist、wir sind、ihr seid、sie/Sie sind。Ich bin müde.＝私は疲れている。Ich bin in Berlin.＝私はベルリンにいる。形が不規則なので、主語との組で覚えます。"),
            section("haben は所有と決まった表現", "haben：ich habe、du hast、er/sie/es hat、wir haben、ihr habt、sie/Sie haben。Ich habe ein Ticket.＝チケットを持っている。ドイツ語では空腹は Ich habe Hunger.、喉の渇きは Ich habe Durst. と表します。"),
        ), listOf(
            example("Ich bin müde.", "私は疲れています。", "Ich＝私 ｜ bin＝sein の ich 形 ｜ müde＝疲れた", "状態を述べるので sein。日本語の『持つ』とは違います。"),
            example("Du hast ein Ticket.", "君はチケットを持っています。", "Du ｜ hast＝haben の du 形 ｜ ein Ticket", "所有を述べるので haben。"),
        ), listOf(
            q("『私は疲れている』。状態を表す文は？", "Ich bin müde.", "私は疲れています。", "状態は sein。主語 ich なので bin を使います。", "Ich habe müde.", "Ich ist müde."),
            q("『君はチケットを持っている』は？", "Du hast ein Ticket.", "君はチケットを持っています。", "持っている＝haben。du に合う不規則形は hast。", "Du bist ein Ticket.", "Du haben ein Ticket."),
            q("『私たちはベルリンにいる』は？", "Wir sind in Berlin.", "私たちはベルリンにいます。", "場所にいる＝sein。主語 wir では sind です。", "Wir ist in Berlin.", "Wir haben in Berlin."),
        ), "sein と haben はあとで過去の文にも使う、とても大切な2語です。"),
        GrammarUnit("foundation-05", "05 名詞は冠詞とセット", "文法上の性・定冠詞・不定冠詞・複数を知る", listOf(
            section("性は単語の分類", "男性 der、女性 die、中性 das。これは文法上の分類で、物に人間の性別があるわけではありません。der Kaffee、die Stadt、das Hotel のように冠詞ごと覚えます。複数の定冠詞は性にかかわらず die：die Hotels。"),
            section("『その』と『あるひとつ』", "話し手と聞き手が特定できるものには定冠詞 der/die/das。初めて出すあるひとつには不定冠詞 ein/eine/ein（主格）。複数に ein の形はなく、不特定なら Hotels のように冠詞なしにもなります。複数形は語ごとに覚えます：die Stadt → die Städte。"),
        ), listOf(
            example("Das ist ein Hotel.", "これはホテルです。", "Das ist＝これは〜です ｜ ein Hotel＝あるひとつのホテル", "Hotel は中性。主格の不定冠詞は ein。"),
            example("Die Hotels sind schön.", "そのホテルたちはすてきです。", "Die Hotels＝複数の主語 ｜ sind＝複数 ｜ schön", "複数の定冠詞は die。動詞も複数形 sind にします。"),
        ), listOf(
            q("Stadt は女性名詞。『これはある街です』は？", "Das ist eine Stadt.", "これは街です。", "女性名詞の主格の不定冠詞は eine。", "Das ist ein Stadt.", "Das ist einen Stadt."),
            q("Hotel は中性名詞。特定のホテルを主語にする文は？", "Das Hotel ist schön.", "そのホテルはすてきです。", "中性の定冠詞は das。Das Hotel が主語です。", "Der Hotel ist schön.", "Die Hotel ist schön."),
            q("『そのホテルたちはすてき』。複数の文は？", "Die Hotels sind schön.", "そのホテルたちはすてきです。", "Hotel の複数は Hotels。複数の定冠詞 die と動詞 sind を組み合わせます。", "Das Hotels ist schön.", "Der Hotels sind schön."),
        ), "単語帳は Hotel だけでなく『das Hotel / die Hotels』で覚える。"),
        GrammarUnit("foundation-06", "06 格①：する人と、される物", "主格と対格を役割で区別する", listOf(
            section("格は文の中の役割", "主格（1格）は主語『だれが・何が』、対格（4格）は多くの動詞の直接の相手『だれを・何を』。Ich sehe den Mann. では Ich が見る人、den Mann が見られる人です。格は単に単語の置き場所だけでは決まりません。"),
            section("まず男性の変化を見る", "主格：der / die / das / die（複数）。対格：den / die / das / die。定冠詞では男性だけ der → den。不定冠詞も男性 ein → einen、女性 eine、中性 ein はこの2つの格では同じ形です。"),
        ), listOf(
            example("Der Mann sieht den Hund.", "その男性はその犬を見ます。", "Der Mann＝主格・見る人 ｜ sieht＝見る ｜ den Hund＝対格・見られる犬", "Mann も Hund も男性名詞ですが、文中の役割で冠詞が変わります。"),
            example("Ich kaufe einen Kaffee.", "私はコーヒーを1杯買います。", "Ich＝主語 ｜ kaufe＝買う ｜ einen Kaffee＝買う対象", "Kaffee は男性名詞。買う対象なので対格 einen。"),
        ), listOf(
            q("男性名詞 Kaffee を『買う対象』にする文は？", "Ich kaufe einen Kaffee.", "私はコーヒーを1杯買います。", "kaufe の対象は対格。男性の ein は einen に変わります。", "Ich kaufe ein Kaffee.", "Ich kaufe einer Kaffee."),
            q("『その男性が、その犬を見る』。冠詞が正しい文は？", "Der Mann sieht den Hund.", "その男性はその犬を見ます。", "見る人は主格 der Mann、見られる犬は対格 den Hund。", "Den Mann sieht der Hund.", "Der Mann sieht der Hund."),
            q("中性名詞 Ticket を買う文は？", "Ich kaufe ein Ticket.", "私はチケットを1枚買います。", "中性の不定冠詞は主格も対格も ein。対格だから必ず einen になるわけではありません。", "Ich kaufe einen Ticket.", "Ich kaufe eine Ticket."),
        ), "格を決める → 名詞の性を確認 → 冠詞を選ぶ。この順で考えます。"),
        GrammarUnit("foundation-07", "07 格②：だれに・だれと", "与格と動詞・前置詞の結びつきを知る", listOf(
            section("与格（3格）は『だれに』が入口", "Ich gebe dem Mann ein Ticket. では、渡す物 ein Ticket が対格、受け取る相手 dem Mann が与格です。ただし日本語の『に』を全部与格に置き換えることはできません。helfen（助ける）のように与格を取る動詞もあります。"),
            section("与格の冠詞と mit", "与格の定冠詞は dem（男性）、der（女性）、dem（中性）、den（複数）。複数名詞には原則 -n も付きますが、すでに -n/-s で終わる形には付けません。不定冠詞は einem / einer / einem。mit（〜と・〜で）は与格を要求します：mit dem Zug。"),
        ), listOf(
            example("Ich gebe dem Mann ein Ticket.", "私はその男性にチケットを渡します。", "Ich＝渡す人 ｜ gebe ｜ dem Mann＝受け取る人・与格 ｜ ein Ticket＝渡す物・対格", "対象が2つあるとき、何を／だれにを分けて考えます。"),
            example("Ich fahre mit dem Zug.", "私は電車で行きます。", "Ich ｜ fahre＝行く ｜ mit＝〜で ｜ dem Zug＝与格", "Zug は男性名詞。mit の後ろは与格なので dem。"),
        ), listOf(
            q("『電車で行く』。mit の後ろの正しい形は？", "Ich fahre mit dem Zug.", "私は電車で行きます。", "mit は与格を取ります。男性 der Zug は dem Zug に。", "Ich fahre mit den Zug.", "Ich fahre mit der Zug."),
            q("『その女性にチケットを渡す』は？", "Ich gebe der Frau ein Ticket.", "私はその女性にチケットを渡します。", "受け取る相手 Frau は与格。女性の与格の定冠詞は der です。der が常に男性とは限りません。", "Ich gebe die Frau ein Ticket.", "Ich gebe dem Frau ein Ticket."),
            q("helfen は与格を取る。『その男性を助ける』は？", "Ich helfe dem Mann.", "私はその男性を助けます。", "日本語は『を』でも、helfen の相手は与格。動詞ごとに必要な格を覚えます。", "Ich helfe den Mann.", "Ich helfe der Mann."),
        ), "主格・対格・与格の名前よりも、文の中での役割を先に見ましょう。"),
        GrammarUnit("foundation-08", "08 質問を組み立てる", "はい・いいえの質問と疑問詞の質問を作る", listOf(
            section("はい・いいえなら動詞を先頭へ", "Du hast ein Ticket.（君はチケットを持っている）を Hast du ein Ticket? にすると『持っていますか？』。活用した動詞 → 主語 → 残り、の順です。ドイツ語では語順自体が質問のしるしになります。"),
            section("疑問詞があるなら疑問詞＋動詞", "wo（どこで・どこに）、wohin（どこへ）、woher（どこから）、was（何）、wer（だれ）、wann（いつ）、wie（どう・どんな）。Wo ist der Bahnhof?＝駅はどこ？疑問詞が第1位、動詞が第2位。wer 自体が主語なら Wer kommt? だけでも文になります。"),
        ), listOf(
            example("Haben Sie ein Ticket?", "チケットをお持ちですか？", "Haben＝活用した動詞 ｜ Sie＝主語 ｜ ein Ticket", "はい・いいえで答える質問なので動詞が先頭。"),
            example("Wo ist der Bahnhof?", "駅はどこですか？", "Wo＝疑問詞 ｜ ist＝動詞 ｜ der Bahnhof＝主語", "疑問詞の質問では動詞が第2位。"),
        ), listOf(
            q("丁寧に『チケットを持っていますか？』と聞く文は？", "Haben Sie ein Ticket?", "チケットをお持ちですか？", "はい・いいえの質問は動詞 Haben を先頭にします。", "Sie haben ein Ticket?", "Sie ein Ticket haben?"),
            q("『駅はどこですか？』の基本の語順は？", "Wo ist der Bahnhof?", "駅はどこですか？", "Wo が第1位、ist が第2位、主語 der Bahnhof が続きます。", "Wo der Bahnhof ist?", "Ist wo der Bahnhof?"),
            q("『君はどこへ行く？』。行き先を聞く文は？", "Wohin gehst du?", "君はどこへ行きますか？", "行き先は wohin。疑問詞 → gehst → du の順です。", "Woher gehst du?", "Wohin du gehst?"),
        ), "平叙文・はい/いいえ疑問文・疑問詞の文を、同じ内容で作り比べよう。"),
        GrammarUnit("foundation-09", "09 ない：nicht と kein", "何を否定するかで否定語を選ぶ", listOf(
            section("名詞の『ない』は kein", "ein/eine が付く名詞や、冠詞なしの名詞を否定するときは kein を使います。Ich habe ein Ticket. → Ich habe kein Ticket. kein は ein と似た格変化をし、男性対格は keinen：Ich trinke keinen Kaffee."),
            section("状態・動作・特定の語を否定する nicht", "Ich bin nicht müde.（疲れていない）は形容詞 müde の前。Ich komme heute nicht.（今日は来ない）は文全体の否定。nicht は否定したい語の前に置くことが多く、常に文末ではありません。特定の名詞や対比では nicht を使う場合もあります。"),
        ), listOf(
            example("Ich habe kein Ticket.", "私はチケットを持っていません。", "Ich ｜ habe ｜ kein Ticket＝チケットがない", "不特定の名詞 ein Ticket を否定するので kein。"),
            example("Ich bin nicht müde.", "私は疲れていません。", "Ich ｜ bin ｜ nicht＝否定 ｜ müde＝状態", "形容詞を否定するので nicht を形容詞の前に。"),
        ), listOf(
            q("『チケットを持っていない』。名詞を否定する文は？", "Ich habe kein Ticket.", "私はチケットを持っていません。", "ein Ticket の否定は kein Ticket。Ticket は中性の対格なので kein。", "Ich habe nicht ein Ticket.", "Ich habe keinen Ticket."),
            q("『疲れていない』。状態を否定する文は？", "Ich bin nicht müde.", "私は疲れていません。", "müde は形容詞なので nicht。kein は名詞と組み合わせます。", "Ich bin kein müde.", "Ich nicht bin müde."),
            q("『コーヒーを飲まない』。不特定の男性名詞を否定するには？", "Ich trinke keinen Kaffee.", "私はコーヒーを飲みません。", "Kaffee は男性で、飲む対象の対格。kein が keinen に変わります。", "Ich trinke kein Kaffee.", "Ich trinke keine Kaffee."),
        ), "否定語を選ぶ前に『名詞がない？ 状態・動作を否定？』を確認。"),
        GrammarUnit("foundation-10", "10 文を挟む助動詞", "できる・必要・したいを動詞の枠で表す", listOf(
            section("活用する助動詞＋文末の不定詞", "können（できる）、müssen（〜しなければならない）、wollen（〜したい）を使うと、助動詞が主語に合わせて第2位、実際の動作を表す不定詞は文末です：Ich kann Deutsch sprechen. この2つで文を挟みます。"),
            section("基本形と否定の違い", "ich/er kann、du kannst、wir/Sie können。ich/er muss、du musst、wir/Sie müssen。ich/er will、du willst、wir/Sie wollen。nicht müssen は『しなくてもよい』で、『してはいけない』ではありません。禁止は nicht dürfen。丁寧な希望には möchte（〜したいのですが）をよく使います。"),
        ), listOf(
            example("Ich kann Deutsch sprechen.", "私はドイツ語を話せます。", "Ich ｜ kann＝できる・第2位 ｜ Deutsch ｜ sprechen＝話す・文末", "sprechen は不定詞のまま。2つとも活用しません。"),
            example("Ich möchte einen Kaffee bestellen.", "私はコーヒーを1杯注文したいです。", "Ich ｜ möchte＝丁寧な希望 ｜ einen Kaffee＝対格 ｜ bestellen＝文末", "旅行の希望も同じ文の枠で作れます。"),
        ), listOf(
            q("『私はドイツ語を話せる』。動詞の枠が正しい文は？", "Ich kann Deutsch sprechen.", "私はドイツ語を話せます。", "kann が第2位、実際の動作 sprechen は文末の不定詞です。", "Ich kann sprechen Deutsch.", "Ich kann Deutsch spreche."),
            q("『君は電車に乗らなければならない』は？", "Du musst den Zug nehmen.", "君はその電車に乗らなければなりません。", "du に合う助動詞は musst。nehmen を不定詞のまま文末に置きます。", "Du muss den Zug nehmen.", "Du musst den Zug nimmst."),
            q("『しなくてもよい』を表す文は？", "Ich muss heute nicht arbeiten.", "私は今日働かなくてもかまいません。", "nicht müssen は義務がないという意味。禁止を表す nicht dürfen と区別します。", "Ich darf heute nicht arbeiten.", "Ich kann heute nicht arbeiten."),
        ), "文末の動詞を読むまで、何をできる・したいのか決まりません。"),
        GrammarUnit("foundation-11", "11 分かれて戻る動詞", "分離動詞の前つづりと文末を理解する", listOf(
            section("an＋kommen＝到着する", "ankommen のような分離動詞は、普通の現在の平叙文で前つづりが離れます：Der Zug kommt an. 活用する本体 kommt は第2位、前つづり an は文末へ。aufstehen（起きる）→ Ich stehe auf. も同じ仕組みです。"),
            section("不定詞ならくっついたまま", "助動詞と一緒なら分離動詞を不定詞のまま文末に置きます：Ich muss früh aufstehen. ここでは auf と stehen を離しません。すべての前つづりが分離するわけではなく、besuchen の be- などは分離しません。"),
        ), listOf(
            example("Der Zug kommt um acht Uhr an.", "その電車は8時に到着します。", "Der Zug ｜ kommt＝本体 ｜ um acht Uhr＝8時に ｜ an＝前つづり", "ankommen の前つづりが文末へ移動しています。"),
            example("Ich muss früh aufstehen.", "私は早く起きなければなりません。", "Ich ｜ muss ｜ früh＝早く ｜ aufstehen＝不定詞", "助動詞の後ろの不定詞は分離せず、ひとまとまり。"),
        ), listOf(
            q("ankommen を使って『電車は8時に到着する』は？", "Der Zug kommt um acht Uhr an.", "その電車は8時に到着します。", "kommt が第2位、分離した an が文末です。", "Der Zug ankommt um acht Uhr.", "Der Zug kommt an um acht Uhr."),
            q("aufstehen を使って『私は早く起きる』は？", "Ich stehe früh auf.", "私は早く起きます。", "本体 stehe を活用し、前つづり auf を文末に置きます。", "Ich aufstehe früh.", "Ich stehe auf früh."),
            q("助動詞 muss と一緒に使う正しい文は？", "Ich muss früh aufstehen.", "私は早く起きなければなりません。", "助動詞が活用を担当するので、aufstehen は不定詞のまま文末に置きます。", "Ich muss früh stehe auf.", "Ich muss auf früh stehen."),
        ), "分離した前つづりは動詞の一部。読み落とすと意味が変わります。"),
        GrammarUnit("foundation-12", "12 場所と行き先の前置詞", "前置詞が要求する格を区別する", listOf(
            section("前置詞は格のスイッチ", "mit・nach・aus・zu・von・bei は与格、für・ohne・durch などは対格。Ich fahre mit dem Zug. と Ich kaufe ein Ticket für den Mann. では前置詞に合わせて冠詞が変わります。zu dem → zum、zu der → zur と縮まります。"),
            section("in/an/auf などは場所か方向か", "in・an・auf などの二格支配前置詞は『どこにいる？』なら与格、『どこへ位置が変わる？』なら対格。in dem Hotel → im Hotel（ホテルの中で）、in das Hotel → ins Hotel（ホテルの中へ）。単に体が動くかどうかではなく、場所の中にいるか、その場所へ移るかを見ます。都市名 Berlin へは普通 nach Berlin。"),
        ), listOf(
            example("Ich bin im Hotel.", "私はホテルの中にいます。", "Ich ｜ bin ｜ im＝in dem ｜ Hotel＝中性の与格", "今いる場所なので与格。"),
            example("Ich gehe ins Hotel.", "私はホテルの中へ入っていきます。", "Ich ｜ gehe ｜ ins＝in das ｜ Hotel＝中性の対格", "ホテルの中へ位置が変わるので対格。zum Hotel はホテルの方へ、という表現です。"),
        ), listOf(
            q("『ホテルの中にいる』。場所を表す文は？", "Ich bin im Hotel.", "私はホテルの中にいます。", "場所の in は与格。in dem Hotel が縮まって im Hotel。", "Ich bin ins Hotel.", "Ich bin in den Hotel."),
            q("『ホテルの中へ入っていく』。方向を表す文は？", "Ich gehe ins Hotel.", "私はホテルの中へ入っていきます。", "中へ位置が変わる in は対格。in das Hotel が縮まって ins Hotel。", "Ich gehe im Hotel.", "Ich gehe in dem Hotel."),
            q("für は対格。『その男性のためにチケットを買う』は？", "Ich kaufe ein Ticket für den Mann.", "私はその男性のためにチケットを買います。", "für の後ろは対格。男性の der Mann が den Mann になります。", "Ich kaufe ein Ticket für dem Mann.", "Ich kaufe ein Ticket für der Mann."),
        ), "動詞の目的語だけでなく、前置詞も名詞の格を決めます。"),
        GrammarUnit("foundation-13", "13 過去を話す：完了形", "haben/sein と過去分詞で過去の出来事を表す", listOf(
            section("現在形の助動詞＋文末の過去分詞", "会話で過去を話すときによく使う完了形：Ich habe einen Kaffee bestellt. haben が主語に合わせて第2位、過去分詞 bestellt が文末です。規則動詞は多くが ge-＋語幹＋-t：machen → gemacht。ただし bestellen のような非分離の前つづり付きや -ieren 動詞には ge- が付きません。"),
            section("sein を使う動詞もある", "場所の移動や状態の変化を表す一部の動詞は sein：gehen → gegangen、ankommen → angekommen。Ich bin nach Berlin gefahren.＝ベルリンへ行った。多くの動詞は haben ですが、単純に『動いたら全部 sein』ではありません。過去分詞と助動詞をセットで覚えます。"),
        ), listOf(
            example("Ich habe einen Kaffee bestellt.", "私はコーヒーを1杯注文しました。", "Ich ｜ habe＝助動詞 ｜ einen Kaffee ｜ bestellt＝過去分詞", "bestellen は非分離の be- が付くので gebestellt にはしません。"),
            example("Ich bin nach Berlin gefahren.", "私はベルリンへ行きました。", "Ich ｜ bin＝sein の ich 形 ｜ nach Berlin ｜ gefahren＝fahren の過去分詞", "ここでは場所を移る意味なので sein を使います。"),
        ), listOf(
            q("『私はコーヒーを注文した』。完了形は？", "Ich habe einen Kaffee bestellt.", "私はコーヒーを1杯注文しました。", "habe が第2位、過去分詞 bestellt は文末。be- 付きなので ge- は付きません。", "Ich habe einen Kaffee bestellen.", "Ich habe einen Kaffee gebestellt."),
            q("『私はベルリンへ行った』。移動の完了形は？", "Ich bin nach Berlin gefahren.", "私はベルリンへ行きました。", "この移動の fahren は sein と過去分詞 gefahren を使います。", "Ich habe nach Berlin gefahren.", "Ich bin nach Berlin fahren."),
            q("machen の規則的な過去分詞を使う文は？", "Ich habe das gemacht.", "私はそれをしました。", "machen の語幹 mach- に ge- と -t：gemacht。", "Ich habe das gemachen.", "Ich habe das machen."),
        ), "現在・助動詞・分離動詞・完了形は、すべて『動詞の枠』につながります。"),
        GrammarUnit("foundation-14", "14 理由をつなぐ：weil", "主文と従属文の語順を区別する", listOf(
            section("weil の中では動詞が最後", "weil（〜だから）は従属文を導きます。Ich lerne Deutsch, weil ich nach Berlin reise. 主文は動詞第2位、weil の中は活用した動詞 reise が最後です。主文と従属文の間にコンマを置きます。dass（〜ということ）などもこの語順です。"),
            section("理由を先に言うこともできる", "Weil ich müde bin, bleibe ich hier.＝疲れているので、私はここにいる。weil の文全体が主文の第1のかたまりになり、コンマの後ろは動詞 bleibe → 主語 ich。助動詞を含む従属文は weil ich Deutsch lernen möchte のように活用した助動詞が最後です。"),
        ), listOf(
            example("Ich bleibe hier, weil ich müde bin.", "私は疲れているので、ここにいます。", "Ich bleibe hier＝主文 ｜ weil ｜ ich müde bin＝理由・bin が最後", "主文と理由の文では、活用した動詞の位置が違います。"),
            example("Weil ich müde bin, bleibe ich hier.", "疲れているので、私はここにいます。", "Weil ich müde bin＝第1のかたまり ｜ bleibe＝主文の第2位 ｜ ich hier", "理由を前に出しても主文の動詞第2位は守ります。"),
        ), listOf(
            q("weil の中で動詞を最後に置いた文は？", "Ich bleibe hier, weil ich müde bin.", "私は疲れているので、ここにいます。", "weil は従属文を導くので、活用した bin を最後に置きます。", "Ich bleibe hier, weil ich bin müde.", "Ich bleibe hier, weil bin ich müde."),
            q("理由の文を先頭に置いた正しい文は？", "Weil ich müde bin, bleibe ich hier.", "疲れているので、私はここにいます。", "weil の文全体が主文の第1位。続く主文は bleibe → ich の順です。", "Weil ich müde bin, ich bleibe hier.", "Weil ich bin müde, bleibe ich hier."),
            q("weil の中に助動詞 möchte がある正しい文は？", "Ich komme, weil ich Deutsch lernen möchte.", "私はドイツ語を学びたいので来ます。", "従属文では活用した助動詞 möchte が最後。不定詞 lernen はその前です。", "Ich komme, weil ich möchte Deutsch lernen.", "Ich komme, weil möchte ich Deutsch lernen."),
        ), "ここまでで文の骨組みがつながります。旅行コースの文も分解してみましょう。"),
    ).also { units ->
        require(units.map { it.id }.distinct().size == units.size)
        require(units.all { unit -> unit.questions.size == 3 && unit.questions.all { q ->
            q.choices.distinct().size == q.choices.size && q.choices.count { it == q.answer } == 1
        } })
    }

    fun unit(id: String) = units.firstOrNull { it.id == id }
    fun question(cardId: String): GrammarQuestion? = units.firstNotNullOfOrNull { unit ->
        unit.questions.indices.firstOrNull { unit.cardId(it) == cardId }?.let { unit.questions[it] }
    }
    fun extend(base: ContentPack) = base.copy(
        cards = base.cards + units.flatMap { it.cards() }, lessons = base.lessons + units.map { it.practiceLesson() },
    )
}
