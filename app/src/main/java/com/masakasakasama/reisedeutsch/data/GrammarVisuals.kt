package com.masakasakasama.reisedeutsch.data

enum class GrammarRole { SUBJECT, VERB, OBJECT, RECIPIENT, CONTEXT, ARTICLE, NEGATION, SOUND, NOUN, ENDING, LINKER }
data class VisualToken(val text: String, val label: String, val role: GrammarRole, val connector: String = "→")
data class VisualRow(val label: String, val tokens: List<VisualToken>, val meaning: String)
data class GrammarVisual(val title: String, val rows: List<VisualRow>, val takeaway: String)

/** Explicit roles, rather than guessing grammar from the spelling of a word. */
object GrammarVisuals {
    private fun t(text: String, label: String, role: GrammarRole, connector: String = "→") = VisualToken(text, label, role, connector)
    private fun row(label: String, meaning: String, vararg tokens: VisualToken) = VisualRow(label, tokens.toList(), meaning)
    private fun diagram(title: String, takeaway: String, vararg rows: VisualRow) = GrammarVisual(title, rows.toList(), takeaway)
    private val s = GrammarRole.SUBJECT
    private val v = GrammarRole.VERB
    private val o = GrammarRole.OBJECT
    private val d = GrammarRole.RECIPIENT
    private val c = GrammarRole.CONTEXT
    private val a = GrammarRole.ARTICLE
    private val n = GrammarRole.NEGATION
    private val sound = GrammarRole.SOUND
    private val noun = GrammarRole.NOUN
    private val ending = GrammarRole.ENDING
    private val linker = GrammarRole.LINKER

    val byUnit = mapOf(
        "foundation-01" to listOf(
            diagram("文字の順番で、音も意味も変わる", "ei と ie を、ひとつの音のまとまりとして見よう。",
                row("ei の組", "Reise は『旅』。", t("ei", "アイに近い音", sound), t("Reise", "ライゼ・旅", noun)),
                row("ie の組", "Riese は『巨人』。", t("ie", "長いイー", sound), t("Riese", "リーゼ・巨人", noun))),
            diagram("名詞を見つけたら、頭を大文字に", "名詞かどうかがポイント。単語を全部大文字にはしません。",
                row("名詞", "Hotel は『ホテル』という物の名前。", t("hotel", "小文字のままは誤り", n), t("Hotel", "名詞の H を大文字に", noun)),
                row("形容詞", "gut は『よい』という状態。", t("gut", "文中では小文字", c), t("Der Kaffee ist gut.", "名詞 Kaffee だけ大文字", c)))
        ),
        "foundation-02" to listOf(
            diagram("動詞の席は、第2のかたまり", "先頭を入れ替えても、赤い動詞は第2位のまま。",
                row("主語から始める", "私は今日コーヒーを飲む。", t("Ich", "① だれが", s), t("trinke", "② どうする", v), t("heute", "③ いつ", c), t("Kaffee", "④ 何を", o)),
                row("時間から始める", "今日、私はコーヒーを飲む。", t("Heute", "① いつ", c), t("trinke", "② どうする", v), t("ich", "③ だれが", s), t("Kaffee", "④ 何を", o))),
            diagram("2語目ではなく、2つ目の箱", "Am Morgen は2語ですが、時間を表すひとつの箱です。",
                row("朝に", "朝、私はコーヒーを飲む。", t("Am Morgen", "① この2語で1箱", c), t("trinke", "② 動詞の席", v), t("ich", "③ 主語", s), t("Kaffee", "④ 対象", o)))
        ),
        "foundation-03" to listOf(
            diagram("主語を決める → 語尾を付ける", "lernen から -en を取ると lern-。語幹は同じで、語尾が変わります。",
                row("私なら", "ich lerne", t("lern", "共通の語幹", v), t("e", "ich の語尾", ending, "＋"), t("lerne", "私が学ぶ", v, "＝")),
                row("君なら", "du lernst", t("lern", "共通の語幹", v), t("st", "du の語尾", ending, "＋"), t("lernst", "君が学ぶ", v, "＝")),
                row("彼・彼女・それなら", "er/sie/es lernt", t("lern", "共通の語幹", v), t("t", "3人称単数の語尾", ending, "＋"), t("lernt", "彼などが学ぶ", v, "＝"))),
            diagram("複数と丁寧な『あなた』", "丁寧な Sie は大文字で、動詞は複数と同じ -en。",
                row("私たち", "私たちは学ぶ。", t("wir", "主語", s), t("lernen", "語尾 -en", v)),
                row("君たち", "君たちは学ぶ。", t("ihr", "主語", s), t("lernt", "語尾 -t", v)),
                row("丁寧なあなた", "あなたは学ぶ。", t("Sie", "丁寧なので大文字", s), t("lernen", "語尾 -en", v)))
        ),
        "foundation-04" to listOf(
            diagram("状態・場所 → sein", "sein は主語に合わせて形が大きく変わるので、組で覚えよう。",
                row("私", "私は疲れている。", t("Ich", "だれが", s), t("bin", "sein の私の形", v), t("müde", "どんな状態", c)),
                row("私たち", "私たちはベルリンにいる。", t("Wir", "だれが", s), t("sind", "sein の私たちの形", v), t("in Berlin", "どこに", c))),
            diagram("持っている → haben", "同じ主語でも、言いたい内容で動詞を選びます。",
                row("私", "私はチケットを持っている。", t("Ich", "だれが", s), t("habe", "持っている", v), t("ein Ticket", "何を", o)),
                row("君", "君はチケットを持っている。", t("Du", "だれが", s), t("hast", "haben の君の形", v), t("ein Ticket", "何を", o)))
        ),
        "foundation-05" to listOf(
            diagram("単語は、この2枚で覚える", "冠詞が名詞の文法上の性を教えてくれます。人間の性別とは別の分類です。",
                row("男性名詞", "コーヒー", t("der", "男性の定冠詞", a), t("Kaffee", "名詞", noun, "＋")),
                row("女性名詞", "街", t("die", "女性の定冠詞", a), t("Stadt", "名詞", noun, "＋")),
                row("中性名詞", "ホテル", t("das", "中性の定冠詞", a), t("Hotel", "名詞", noun, "＋"))),
            diagram("1つから複数へ", "複数の定冠詞は die。名詞の複数形は単語ごとに覚えます。",
                row("ホテル", "中性の単数から複数。", t("das Hotel", "1つ・単数", noun), t("die Hotels", "複数・末尾 s", noun)),
                row("街", "女性の単数から複数。", t("die Stadt", "1つ・単数", noun), t("die Städte", "複数・ä と e", noun)))
        ),
        "foundation-06" to listOf(
            diagram("する人と、される対象を分ける", "青＝主語、黄＝対象。同じ男性名詞でも役割が違います。",
                row("見る方向", "その男性が、その犬を見る。", t("Der Mann", "① 見る人・主格", s), t("sieht", "② 見る", v), t("den Hund", "③ 見られる犬・対格", o))),
            diagram("男性の冠詞は、対格で変わる", "女性 die/eine と中性 das/ein は、この2つの格では同じ形です。",
                row("特定のもの", "男性の定冠詞。", t("der", "主格・だれが", s), t("den", "対格・何を", o)),
                row("あるひとつ", "男性の不定冠詞。", t("ein", "主格・だれが", s), t("einen", "対格・何を", o)),
                row("コーヒーを買う", "買う対象なので einen。", t("Ich", "主語", s), t("kaufe", "買う", v), t("einen Kaffee", "男性の対格", o)))
        ),
        "foundation-07" to listOf(
            diagram("渡す人 → 受け手 → 渡す物", "緑＝だれに（与格）、黄＝何を（対格）。相手と物を別の箱に。",
                row("チケットを渡す", "私はその男性にチケットを渡す。", t("Ich", "① 渡す人・主格", s), t("gebe", "② 渡す", v), t("dem Mann", "③ 受け手・与格", d), t("ein Ticket", "④ 渡す物・対格", o))),
            diagram("mit の後ろは、与格の箱", "日本語の『を・に』だけでは決まりません。前置詞・動詞の指定も見よう。",
                row("電車で", "mit は与格を要求する。", t("mit", "与格を指定", d), t("dem Zug", "男性の与格", d, "＋")),
                row("助ける相手", "helfen の相手も与格。", t("Ich", "主語", s), t("helfe", "助ける", v), t("dem Mann", "日本語は『を』でも与格", d)))
        ),
        "foundation-08" to listOf(
            diagram("はい・いいえの質問：動詞が先頭へ", "赤い動詞を前に出すと、質問の基本形になります。",
                row("普通の文", "あなたはチケットを持っています。", t("Sie", "① 主語", s), t("haben", "② 動詞", v), t("ein Ticket", "③ 対象", o)),
                row("質問", "チケットをお持ちですか？", t("Haben", "① 動詞を先頭に", v), t("Sie", "② 主語", s), t("ein Ticket?", "③ 対象", o))),
            diagram("疑問詞があるなら、その後ろに動詞", "Wo は場所、Wohin は行き先。何を知りたいかで選びます。",
                row("駅はどこ？", "駅はどこですか？", t("Wo", "① 疑問詞・場所", c), t("ist", "② 動詞", v), t("der Bahnhof?", "③ 主語", s)),
                row("どこへ行く？", "君はどこへ行く？", t("Wohin", "① 疑問詞・行き先", c), t("gehst", "② 動詞", v), t("du?", "③ 主語", s)))
        ),
        "foundation-09" to listOf(
            diagram("名詞の『ない』→ kein の箱", "まず否定する名詞を探し、その性と格に合わせます。",
                row("ある", "チケットを持っている。", t("Ich", "主語", s), t("habe", "持っている", v), t("ein Ticket", "ある1枚", o)),
                row("ない", "チケットを持っていない。", t("Ich", "主語", s), t("habe", "持っている", v), t("kein Ticket", "名詞を否定", n))),
            diagram("状態を否定 → nicht を前に", "否定したい状態の前に nicht。名詞用の kein とは区別。",
                row("疲れている", "肯定の状態。", t("Ich", "主語", s), t("bin", "状態をつなぐ", v), t("müde", "疲れた状態", c)),
                row("疲れていない", "状態を否定。", t("Ich", "主語", s), t("bin", "状態をつなぐ", v), t("nicht", "後ろの状態を否定", n), t("müde", "否定する状態", c)))
        ),
        "foundation-10" to listOf(
            diagram("2つの動詞で、文を挟む", "助動詞を活用して第2位に置き、実際の動作は不定詞で文末へ。",
                row("できる", "私はドイツ語を話せる。", t("Ich", "① 主語", s), t("kann", "② 枠の左・活用", v), t("Deutsch", "中身・何を", o), t("sprechen", "枠の右・文末", v)),
                row("したい", "私はコーヒーを注文したい。", t("Ich", "① 主語", s), t("möchte", "② 枠の左・希望", v), t("einen Kaffee", "中身・対格", o), t("bestellen", "枠の右・不定詞", v))),
            diagram("『義務がない』と『禁止』は別", "否定される助動詞が違うので、意味も変わります。",
                row("しなくてもよい", "今日は働く義務がない。", t("muss", "しなければならない", v), t("nicht", "義務を否定", n, "＋")),
                row("してはいけない", "今日は働くことが許されない。", t("darf", "してもよい", v), t("nicht", "許可を否定", n, "＋")))
        ),
        "foundation-11" to listOf(
            diagram("分離動詞：前の部品が文末へ", "ankommen が分かれ、活用する kommt と文末の an になります。",
                row("辞書の形", "到着する。", t("an", "前つづり", v), t("kommen", "本体", v, "＋"), t("ankommen", "辞書の1語", v, "＝")),
                row("普通の文", "電車は8時に到着する。", t("Der Zug", "① 主語", s), t("kommt", "② 本体を活用", v), t("um acht Uhr", "中身・時刻", c), t("an", "離れた部品・文末", v))),
            diagram("助動詞があるなら、くっついたまま", "muss が活用を担当するので、aufstehen は分離しない不定詞です。",
                row("助動詞なし", "私は早く起きる。", t("Ich", "主語", s), t("stehe", "本体を活用", v), t("früh", "早く", c), t("auf", "分離した部品", v)),
                row("助動詞あり", "私は早く起きる必要がある。", t("Ich", "主語", s), t("muss", "こちらを活用", v), t("früh", "早く", c), t("aufstehen", "くっついた不定詞", v)))
        ),
        "foundation-12" to listOf(
            diagram("ホテルの中にいる？ 中へ移る？", "in の後ろは『場所』なら与格、『中へ移る方向』なら対格。",
                row("📍 すでに中にいる", "私はホテルの中にいる。", t("Ich", "主語", s), t("bin", "いる", v), t("im Hotel", "in dem・与格", d)),
                row("🚶 外 → ホテルの中", "私はホテルの中へ入る。", t("Ich", "主語", s), t("gehe", "行く", v), t("ins Hotel", "in das・対格", o))),
            diagram("前置詞が、後ろの格を指定する", "冠詞を選ぶ前に、前置詞の指定を見る。",
                row("mit＋与格", "電車で。", t("mit", "与格を指定", d), t("dem Zug", "男性の与格", d, "＋")),
                row("für＋対格", "その男性のために。", t("für", "対格を指定", o), t("den Mann", "男性の対格", o, "＋")))
        ),
        "foundation-13" to listOf(
            diagram("完了形も、動詞で文を挟む", "左は主語に合わせる助動詞、右は過去分詞。",
                row("現在", "私はコーヒーを注文する。", t("Ich", "主語", s), t("bestelle", "現在の動詞・第2位", v), t("einen Kaffee", "対格の対象", o)),
                row("過去", "私はコーヒーを注文した。", t("Ich", "主語", s), t("habe", "枠の左・第2位", v), t("einen Kaffee", "中身・対格", o), t("bestellt", "枠の右・過去分詞", v))),
            diagram("過去を作る、2つの組", "助動詞と過去分詞をセットで覚える。すべての動作が sein ではありません。",
                row("注文した", "bestellen → hat bestellt。", t("habe", "haben を活用", v), t("bestellt", "注文の過去分詞", v, "＋")),
                row("ベルリンへ移動した", "fahren → ist gefahren（この移動の意味）。", t("bin", "sein を活用", v), t("nach Berlin", "行き先", c), t("gefahren", "移動の過去分詞", v)))
        ),
        "foundation-14" to listOf(
            diagram("weil の中では、動詞が最後へ", "同じ『疲れている』でも、主文と従属文で動詞の席が違います。",
                row("主文", "私は疲れている。", t("Ich", "① 主語", s), t("bin", "② 動詞", v), t("müde", "状態", c)),
                row("weil の理由の文", "私が疲れているので。", t("weil", "理由の入口", linker), t("ich", "主語", s), t("müde", "状態", c), t("bin", "動詞を最後へ", v))),
            diagram("理由の文全体で、ひとつの箱", "理由を先に言っても、主文の動詞 bleibe は第2の箱です。",
                row("理由を先に", "疲れているので、私はここにいる。", t("Weil ich müde bin,", "① 理由の文で1箱", c), t("bleibe", "② 主文の動詞", v), t("ich", "主語", s), t("hier", "ここに", c)))
        )
    )
}
