# Lunaへの実装引き継ぎ

## 依頼

**このリポジトリの設計を基にReiseDeutschのAndroidアプリを実装する。** ユーザーは設計を本担当、実装をLunaに任せると明示した。このファイルは別の実装セッションへそのまま渡せる。

作業repo: `masakasakasama/German-language`。ローカル環境は現在 `/workspace/German-language`。正式画像は [reference/reisedeutsch.jpg](reference/reisedeutsch.jpg)。既存アプリの学習ロジック流用と、画像の旅行うさぎ/水彩/カード/会話/地図の見た目を必須条件とする。

## 読む順番

1. PRODUCT.md、UI.md、正式画像。
2. LEARNING.md、reference/legacy-srs.js、reference/srs-fixtures.json。
3. CONTENT.md、ARCHITECTURE.md。
4. RELEASE.md、ACCEPTANCE.md、SOURCES.md。
5. root CODEX_STATE.mdと実行環境にある適用範囲のAGENTS.md。

仕様間の優先関係はSOURCES参照。既存実装と意図的な修正を区別し、独自の学習アルゴリズムや汎用のカード画面へ置換しない。今のrepoには設計資料だけで、アプリができているとは仮定しない。

## 実装の順序と成果物

| 段階 | 作業 | その段階の確認 |
|---|---|---|
| 1 | Android project、version catalog、wrapper、Room/DataStore、固定revision共通token、ReiseTheme | debug build、単体test実行環境 |
| 2 | Scheduler/Grader/イベント保存/報酬、L02カフェ1レッスン、紹介/練習/復習/再開 | fixture一致、二重更新防止、終了して再開 |
| 3 | 個別うさぎ/街/場面素材、ホーム・会話・地図の3画面を画像どおり組む | 実際のスクリーンショットを参考画像と比較 |
| 4 | 20レッスン/60フレーズ/30語彙/12会話JSON、コレクション、都市進捗、学習記録 | 教材validator、主導線、実データ反映 |
| 5 | TTS/低速/音声認識/文字代替、設定、通知、バックアップ/復元 | 拒否/オフライン/不正backup、データ保持 |
| 6 | GitHub feed client、APK検証、OS更新、release workflow | update失敗ケース、同鍵上書き、全受入確認 |

最初の縦の導線は「ホーム→カフェL02→フレーズ紹介→自己評価→練習→保存→終了→復習」。全画面の空UIを先に並べるだけで終えない。段階3で見た目を固め、最後まで標準Materialの仮画面を残さない。

## 実装で固定すること

- パッケージはcom.masakasakasama.reisedeutsch、minSdk26、端末内保存。
- 自己評価はわからない/少しわかる/わかる。原applyMarkの間隔と係数を維持。
- INTRO評価は1回だけ更新、客観回答とは別イベント。押し直しはbeforeStateから置換。
- reps=0の誤答済みカードも苦手対象。未学習カードは期限件数に含めない。
- 進捗/XP/収集/日次は同一の保存事実から算出し、サンプル数値を埋め込まない。
- 日付は注入Clock/ZoneId、LocalDate。夏時間を24時間の差分で処理しない。
- 録音非対応/拒否でも文字で進める。認識だけで発音点を出さない。
- 画像内のスマホ枠やポスター文字をそのままアプリに貼らない。別素材と実UIで再構成。
- 動作するDB migration、backup、固定署名上書き。進捗を消して更新しない。

## 外部条件と独立して進める範囲

Notion本文未取得。URLと調査結果はSOURCESにある。照合ができるまでローカル実装/検証は進め、公開更新方法をNotion準拠と称さない。

署名鍵/Secrets未確認。先にdebug buildとfeed/clientのテストを行う。本番配布鍵を確保していない状態でreleaseの完成を主張しない。ユーザーに鍵をチャットへ貼らせず、環境/Actionsの安全な設定経路を使う。

個別イラスト/同梱音声は未制作。UI.mdの素材契約を実装工程で満たす。参考画像だけを納品素材扱いしない。実機がなければ自動検証とエミュレータを終え、実機未確認の項目を具体的に残す。

## 確認と報告

ACCEPTANCEのA01〜A20を台帳にし、pass/fail/未実行/外部条件待ちを記録する。自動テストは実際の必要動作を確認する。build、音声、画面、更新、データ保持は確認方法が違うので一括で「全部OK」と報告しない。

引き継ぎ/終了時はCODEX_STATEにDone/Current/Next/Blockers/Verificationと日時を保存する。既存ファイル/ユーザー変更を保持。releaseや他repo変更はその操作の権限を確認してから実施する。今回は本担当はコード実装・commit/push・公開・Luna agent起動を行っていない。

## 開始用プロンプト

> German-languageのdocs/design/LUNA_HANDOFF.mdと指定画像を読み、保存済みのReiseDeutsch設計に沿ってAndroidアプリを実装してください。既存Language_learningの3段階自己評価・SRSを移植し、明記された二重更新等の修正を適用してください。旅行うさぎと水彩の指定デザインを実UIで再現し、受入条件に沿って検証してください。Notion本文、正式署名鍵、実機などの外部条件が不足していても独立した実装を進め、未確認を成功扱いにしないでください。
