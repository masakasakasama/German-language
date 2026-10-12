# 教材設計 v1

## 範囲と出典

20レッスン×3フレーズ=60フレーズ、補助語彙30カード、4場面×3会話。以下の内容はv1の教材ターゲットとして決定する。新規の教材仕様であり、既存Appにこの20レッスンが既にあるという意味ではない。

既存 `Language_learning/js/data/de.js` の語彙・例文・日本語訳を優先再利用する。元カードidをlegacyIdへ保存する。既存教材はAI作成例文を含み、未確認の全データを一括で製品へ載せない。下記ターゲットと合う部分を抽出し、格・性・自然さ・訳を検証してから同梱する。

「Ich hätte gern…」などの旅行定型句は、その文法体系をすべて学習済みとせず、表現として導入できる。教材ラベルA1は初級向けの目安であり、収録文がA1文法だけという保証ではない。都市は学習ストーリーで、文法の説明は実用場面に付随する。

## コースと60フレーズ

IDは `rd:l01`〜`rd:l20`、フレーズは `rd:l01:p01` の形式で固定。下表の各行に3枚。元のカードをそのまま採用する場合は元idを主idにしてもよいが、ターゲットとのmappingを固定する。

| ID・都市・場面 | タイトル | フレーズ1 | フレーズ2 | フレーズ3 | 文法ポイント |
|---|---|---|---|---|---|
| L01 Hamburg・観光 | はじめの挨拶 | Guten Morgen!／おはようございます | Guten Tag!／こんにちは | Auf Wiedersehen!／さようなら | 時間別挨拶、丁寧な挨拶 |
| L02 Hamburg・カフェ | コーヒーを頼む | Ich hätte gern einen Kaffee, bitte.／コーヒーを1杯お願いします | Einen Tee, bitte.／紅茶を1杯お願いします | Danke schön!／ありがとうございます | 男性名詞の4格、定型表現 |
| L03 Hamburg・駅 | 駅を探す | Wo ist der Bahnhof?／駅はどこですか | Ist das der richtige Zug?／これは正しい列車ですか | Danke für Ihre Hilfe.／助けていただきありがとうございます | wo、sein、丁寧なIhre |
| L04 Hamburg・ホテル | 予約を伝える | Ich habe eine Reservierung.／予約しています | Mein Name ist Tanaka.／名前は田中です | Wo ist mein Zimmer?／私の部屋はどこですか | haben、名前、所有詞 |
| L05 Berlin・観光 | 自己紹介 | Ich komme aus Japan.／日本から来ました | Ich lerne Deutsch.／ドイツ語を勉強しています | Sprechen Sie Englisch?／英語を話せますか | 動詞2位、Sie疑問文 |
| L06 Berlin・駅 | 切符を買う | Eine Fahrkarte nach Berlin, bitte.／ベルリン行きの切符を1枚お願いします | Nur hin, bitte.／片道でお願いします | Was kostet die Fahrkarte?／切符はいくらですか | nach＋都市、疑問詞was |
| L07 Berlin・カフェ | 個数と値段 | Zwei Brötchen, bitte.／パンを2つお願いします | Wie viel kostet das?／これはいくらですか | Das ist alles, danke.／以上です、ありがとう | 数字、名詞の複数、wie viel |
| L08 Berlin・ホテル | チェックイン | Kann ich jetzt einchecken?／今チェックインできますか | Hier ist mein Pass.／こちらがパスポートです | Können Sie das bitte wiederholen?／もう一度言っていただけますか | kann/können、動詞末尾 |
| L09 Köln・観光 | 道を尋ねる | Wie komme ich zum Dom?／大聖堂へはどう行けばいいですか | Ist es weit von hier?／ここから遠いですか | Ich verstehe nicht.／わかりません | zu dem→zum、否定nicht |
| L10 Köln・カフェ | 好みを伝える | Ohne Milch, bitte.／牛乳なしでお願いします | Mit Zucker, bitte.／砂糖入りでお願いします | Zum Mitnehmen, bitte.／持ち帰りでお願いします | mit/ohneの導入、定型句 |
| L11 Köln・ホテル | 滞在の確認 | Ist das Frühstück inklusive?／朝食は含まれていますか | Wann gibt es Frühstück?／朝食は何時ですか | Ich bleibe zwei Nächte.／2泊します | wann、es gibt、数量 |
| L12 Köln・駅 | 発車時刻 | Wann fährt der Zug ab?／列車はいつ出発しますか | Von welchem Gleis?／何番線からですか | Hat der Zug Verspätung?／列車は遅れていますか | 分離動詞abfahren、時刻 |
| L13 Frankfurt・観光 | 見学する | Wo kann ich eine Eintrittskarte kaufen?／入場券はどこで買えますか | Darf ich hier fotografieren?／ここで写真を撮ってもいいですか | Wann öffnet das Museum?／博物館はいつ開きますか | können/dürfen、語順 |
| L14 Frankfurt・カフェ | 会計する | Die Rechnung, bitte.／お会計をお願いします | Kann ich mit Karte bezahlen?／カードで払えますか | Kann ich bar bezahlen?／現金で払えますか | 支払い表現、mit |
| L15 Frankfurt・ホテル | 困ったことを伝える | Das WLAN funktioniert nicht.／Wi-Fiが使えません | Ich brauche ein Handtuch.／タオルが必要です | Können Sie mir bitte helfen?／助けていただけますか | brauchen＋4格、mirの導入 |
| L16 Frankfurt・駅 | 乗り換える | Muss ich umsteigen?／乗り換えが必要ですか | Wo muss ich umsteigen?／どこで乗り換えますか | Wann kommen wir an?／いつ到着しますか | müssen、分離動詞ankommen |
| L17 München・観光 | 街歩きと買い物 | Wo ist der Marienplatz?／マリエン広場はどこですか | Ich suche ein Souvenir.／お土産を探しています | Kann ich das anprobieren?／これを試着できますか | suchen＋4格、anprobieren |
| L18 München・カフェ | 食事を頼む | Einen Tisch für zwei Personen, bitte.／2人用の席をお願いします | Haben Sie etwas ohne Fleisch?／肉を使っていないものはありますか | Ein Wasser, bitte.／水を1つお願いします | für＋4格、飲食物の注文 |
| L19 München・ホテル | チェックアウト | Ich möchte auschecken.／チェックアウトしたいです | Kann ich mein Gepäck hier lassen?／ここに荷物を置いておけますか | Vielen Dank für alles.／いろいろありがとうございました | möchte、動詞末尾、所有詞 |
| L20 München・駅 | 帰りの移動 | Wie komme ich zum Flughafen?／空港へはどう行けばいいですか | Ist dieser Platz frei?／この席は空いていますか | Entschuldigung, ich brauche Hilfe.／すみません、助けが必要です | dieser、場所の質問、総復習 |

市の順番は固定。市内の4レッスンは自由順で受講できる。次都市の正式開放は前都市4レッスン完了後。ホームの場面一覧には開放状態を明示し、将来レッスンはプレビュー可能。復習・コレクションには到達済みカードの都市制限をかけない。

## 補助語彙30枚

各IDは `rd:v:<lemma>` にする。名詞は必ず冠詞/単数/複数を表示し、非可算なら「通常、複数形なし」と明示。以下24名詞＋6動詞をv1で収録する。必要な関連レッスンで任意に開け、補助語彙の完了は都市到達の条件にしない。

| 単数・冠詞 | 複数/性質 | 日本語 |
|---|---|---|
| der Kaffee | die Kaffees | コーヒー |
| der Tee | die Tees | 紅茶・茶 |
| das Brötchen | die Brötchen | 小型のパン |
| die Milch | 通常、複数形なし | 牛乳 |
| der Zucker | 通常、複数形なし | 砂糖 |
| die Rechnung | die Rechnungen | 会計・請求書 |
| die Karte | die Karten | カード |
| das Wasser | ここでは飲み物の非可算名詞 | 水 |
| der Tisch | die Tische | テーブル |
| der Bahnhof | die Bahnhöfe | 駅 |
| der Zug | die Züge | 列車 |
| die Fahrkarte | die Fahrkarten | 切符 |
| das Gleis | die Gleise | 番線・線路 |
| die Verspätung | die Verspätungen | 遅れ |
| der Flughafen | die Flughäfen | 空港 |
| der Platz | die Plätze | 席・場所 |
| das Zimmer | die Zimmer | 部屋 |
| die Reservierung | die Reservierungen | 予約 |
| der Pass | die Pässe | パスポート |
| das Handtuch | die Handtücher | タオル |
| das Gepäck | 通常、複数形なし | 荷物 |
| das Museum | die Museen | 博物館 |
| die Eintrittskarte | die Eintrittskarten | 入場券 |
| das Souvenir | die Souvenirs | お土産 |

動詞: sein（〜である）、haben（持っている）、sprechen（話す）、kaufen（買う）、bezahlen（支払う）、helfen（助ける）。表示は不定詞・教材で使う活用形・日本語訳・短い例文。動詞の全活用表や全時制を必須暗記にしない。

## 各レッスンの構成

1. 要点を日本語で2〜4文、例文を1つ。
2. 3フレーズの紹介（訳を開く＋自己評価）。
3. 3練習（1フレーズに最低1問）。
4. 必要な再練習、完了画面。

練習配分は、L01〜04: 意味選択2＋短文入力1、L05〜08: 聞き取り選択1＋穴埋め1＋短文入力1、L09〜12: 語順1＋穴埋め1＋聞き取り1、L13〜20: 語順1＋短文入力1＋聞き取り1。音声がない場合、聞き取りは本文を使う意味選択へ切替し、統計上は聞き取り成功にしない。

全exerciseに正解/別解/解説を保存する。選択肢をランダムな異品詞で水増ししない。教材内で同じ意味の正答が複数ある場合は多重正答か設問修正。短文入力の許容別解は教材に明記する。例えばgern/gerneの置換は該当文でだけ許容。

## 12の固定会話

同じ会話を聞く/文字/話すで切り替える。ターゲットは上記60フレーズから選ぶ。相手の発話は収集60枚に含めないが訳と音声対象にする。

| 会話ID | 場面 | 相手の問い | 学習者のターゲット | 相手の応答 |
|---|---|---|---|---|
| cafe-01 | カフェ | Was kann ich für Sie tun? | Ich hätte gern einen Kaffee, bitte. | Sehr gern! |
| cafe-02 | カフェ | Möchten Sie Milch dazu? | Ohne Milch, bitte. | Natürlich. |
| cafe-03 | カフェ | Möchten Sie bezahlen? | Kann ich mit Karte bezahlen? | Ja, das geht. |
| station-01 | 駅 | Wohin möchten Sie fahren? | Eine Fahrkarte nach Berlin, bitte. | Einfach oder hin und zurück? |
| station-02 | 駅 | Haben Sie eine Frage? | Wann fährt der Zug ab? | Der Zug fährt um zehn Uhr ab. |
| station-03 | 駅 | Kann ich Ihnen helfen? | Muss ich umsteigen? | Ja, einmal in Nürnberg. |
| hotel-01 | ホテル | Guten Tag! Wie kann ich Ihnen helfen? | Ich habe eine Reservierung. | Auf welchen Namen? |
| hotel-02 | ホテル | Haben Sie noch eine Frage? | Wann gibt es Frühstück? | Von sieben bis zehn Uhr. |
| hotel-03 | ホテル | Möchten Sie auschecken? | Ich möchte auschecken. | Sehr gern. Haben Sie den Schlüssel? |
| sightseeing-01 | 観光 | Kann ich Ihnen helfen? | Wie komme ich zum Dom? | Gehen Sie geradeaus. |
| sightseeing-02 | 観光 | Haben Sie eine Frage? | Darf ich hier fotografieren? | Ja, aber bitte ohne Blitz. |
| sightseeing-03 | 観光 | Suchen Sie etwas Bestimmtes? | Ich suche ein Souvenir. | Schauen Sie gern hier. |

訳は自然な日本語で全ターンに付ける。Lunaが教材JSON化するとき、相手の全文訳と発話者名を省略しない。「station-01」の3ターン後には関連表現「Nur hin, bitte.」を表示する。未開放の会話でも試せるが、対応レッスン未紹介のカードは会話を完了した時点で収集/learned状態を作る。

## 教材のレビュー条件

- 冠詞、格、複数形、ドイツ語の大文字、自然な丁寧表現、訳の意味を確認。
- 駅員/ホテル/カフェの会話ではSieを基本にし、duを混ぜる場合は場面を明示。
- 既存EXAMPLESのrequiresCardIds条件を引き継ぎ、追加例文は必要語彙が学習済みの場合に表示。教材の主フレーズとその訳はこの条件で隠さない。
- 元教材の英語訳を日本語UIへ代用表示しない。日本語訳の欠落は教材検証失敗。
- ユーザー指定画像のフレーズの誤植や不自然な日本語はそのまま収録しない。
- 母語話者/専門家による確認ができた場合は記録。未実施なら「母語話者レビュー済み」とは記載しない。
