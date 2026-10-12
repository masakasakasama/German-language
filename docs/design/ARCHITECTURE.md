# データ・実装構成

## 技術決定

Androidネイティブ、Kotlin、Jetpack Compose/Material3、Navigation Compose、ViewModel＋StateFlow、Coroutines、Room、DataStore、WorkManager。初版は単一appモジュールと固定revisionのローカルdesign-systemライブラリ。複数サービス、WebView、サーバー、DIフレームワークは不要。依存注入はAppContainerで構成する。

applicationId/namespaceは `com.masakasakasama.reisedeutsch`。debugは `.debug` suffix、本配布releaseとは共存させる。minSdk26。初期のcompileSdk/targetSdkは35（サイドロード配布）とし、Lunaが選定した公式対応AGP/Kotlin/Compose/JDK17の組をversion catalogとwrapperに固定してbuild検証する。既存AAOS/Design_systemのSDK34・Kotlin1.9.24を無検証で最新依存と混ぜない。SDK変更は理由とビルド結果を引き継ぎに残す。ストア公開はv1範囲外。

共通Design_systemは `c1e1b53c7dc802cb8f62db73f16b7a0d29fd9f83` を起点に必要なAndroid source/生成tokenをvendor配置し、source SHA/hash manifestを保存する。構築時の外部Git master追従はしない。学習ロジックは同様に参照元を記録するが、実行にはKotlin移植を使う。

## 構成案

```
app/src/main/java/com/masakasakasama/reisedeutsch/
  app/            MainActivity, AppContainer, Navigation
  domain/         Scheduler, ReviewQueue, Grader, Rewards, Curriculum
  data/content/   ContentLoader, ContentValidator
  data/local/     AppDatabase, entities, dao, migrations
  data/backup/    BackupRepository
  data/audio/     AudioPlayer, SpeechInput
  data/update/    UpdateRepository, ApkVerifier, InstallLauncher
  ui/theme/       ReiseTheme, app aliases
  ui/components/  Mascot, SpeechBubble, PhraseCard, AssessmentRow, MapNode
  ui/screens/     各S00〜S15
  workers/        ReminderWorker
assets/           content-v1.json, illustrations, optional audio
vendor/tatsu/     pinned source, revision.json, licenses
```

UI→ViewModel→ユースケース→Repositoryの依存方向。Room/OS音声/HTTPを純粋なScheduler/Graderに持ち込まない。画面のViewModelはRepositoryの真の状態を購読し、別のXPや復習件数を独立保管しない。

## 教材契約

教材JSONはschemaVersion=1、contentVersion="1.0.0"、language="de"、uiLanguage="ja"。ルートはcities[], lessons[], cards[], grammarNotes[], conversations[]。unknown major schemaは拒否。asset参照は同梱ファイルだけで、教材から任意コードや任意URLを実行しない。

Card: id、legacyId?、kind(PHRASE/VOCAB)、levelLabel、scene、german、japanese、speakText、grammarNoteIds[]、example{german,japanese,requiresCardIds[]}、noun?{article,plural}、acceptedAnswers[]、normalization、audioAsset?、imageAsset?。

Lesson: id、cityId、scene、order、titleJa、phraseCardIds（必ず3）、supportCardIds[]、grammarNoteIds[]、exerciseSpecs[]。ExerciseSpec: id、cardId、kind、promptJa、options[{id,text}]、acceptedOptionIds/acceptedTokenOrders/acceptedAnswers、normalization、explanationJa。穴埋めと選択は曖昧さ検査を行う。

City: id、nameDe、nameJa、order、mapX/mapY、lessonIds（4件）。Conversation: id、scene、titleJa、turns[{id,speaker,cardId?,german,japanese,audioAsset?}]、responseExercise。教材IDは表示文・配列indexを使わず安定したID。既存カードは元idを保存し、必要ならアプリIDとlegacyIdで対応する。

検証: ID重複0、参照欠落0、20レッスン/60フレーズ/30補助語彙、全フレーズに訳と正解、全exerciseに解説、全会話に代替回答、存在しないasset参照0。JSONは信頼できる初期同梱データでも検証してから有効にする。

## 保存契約（Room v1）

| Entity | キーと主なフィールド |
|---|---|
| ContentMeta | singleton、schemaVersion、contentVersion、activatedAt |
| CardState | cardId PK、assessment?、learnedAt?、collectedAt?、ease、intervalDays、dueLocalDate、scheduleZone、reps、lapses、lastReviewedAt |
| AnswerEvent | eventId PK、sessionId、stepId、cardId、type、assessment?/correct?、hintUsed、atUtc、localDate、zoneId、beforeStateJson、afterStateJson、revision |
| LessonProgress | lessonId PK、introducedCardIds、practicedCardIds、completedAt? |
| Session | sessionId PK、mode、lessonId?、status、queueJson、cursor、createdAt、updatedAt、savedUiStateJson |
| RewardGrant | rewardKey PK、eventId?、kind、xp、localDate。キーで重複加算を防止 |
| DailyMission | localDate PK、missionId、mode、cardIds、completedCardIds |

日次件数、総XP、連続日数、都市完了は上記から集計する。必要ならDB viewを使い、同じ値を非同期で二重保存しない。SettingsはDataStore（theme、dailyGoal、reminderEnabled/time、audioSpeed、translationVisible、reducedMotion、onboarded）。設定と学習DBをバックアップでまとめる。

回答保存は1トランザクションでAnswerEvent→CardState→LessonProgress→RewardGrant→Session.cursor→Missionを更新。二度押し/再送には同じeventIdを渡す。同じeventId・同じ入力は無変更、評価差し替えはbeforeStateから再計算し同じイベントを置換。前のイベントを変える操作は次へ移動するまでに限定し、後続イベントの上書きは行わない。

成功UIはDBコミット後に表示。save failureはbeforeStateを保持し再試行。プロセス終了後はSessionの未完了stepを復元。画面の一時的な選択や認識テキストはSavedStateHandle、回答済み状態はDBが正本。アプリの強制終了で回答済みstepをもう一度加算しない。

## 教材/アプリ移行

Room migrationは破壊的fallback禁止。contentVersion更新は教材定義を置換するが学習記録を保持。カード削除はorphanとして履歴を保存、復習キューから除外。ID変更にはmigration mappingを付ける。学習途中の教材更新はセッション終了まで旧教材スナップショットを保持するか、再開時にmappingで解決し、解決不能stepだけ明示的にスキップして履歴を残す。

新端末へのWeb版履歴の完全移行はv1範囲外。旧mochi.v1を新DBと同一だと仮定しない。将来はprofile/lang/cardIdのmappingを明示する。補助語彙とフレーズのSRSはカード単位を共有し、別の技能モデルへ勝手に置換しない。

## バックアップ

Storage Access FrameworkでJSON保存/選択。format="reisedeutsch-backup"、schemaVersion=1、contentVersion、exportedAt、settings、cardStates、answerEvents、lessonProgress、rewardGrants、dailyMissions、activeSession。画像/音声/署名鍵/更新APKは含めない。最大20MB、上限100,000イベント。完全履歴は日付フィルタなく保持し、サイズ上限超過時は分割方法を表示して既存DBを変更しない。

復元は形式・サイズ・ID・SRS範囲・日付・参照整合性を仮領域で検証→現在データを自動バックアップ→件数/日時を提示→本人確認→単一トランザクションで置換。部分復元/暗黙マージはしない。新しい未対応schemaは理由付きで拒否。未知教材カードはorphan保存して復習対象外にする。完了後に集計再計算、再起動不要。進捗リセットは現在データのエクスポート入口＋明示確認、同梱教材と音声は消さない。

## 通知・データ送信

ReminderWorkerは端末時刻の日次通知、Androidの厳密な時刻保証はせず「指定時刻ごろ」。タイムゾーン変更で再予約。期限カードか未達目標がある日に1回、達成済みは通知しない。通知からS07/今日のミッションへ。許可拒否ならアプリ内だけ。

インターネットは更新確認/音声認識サービスで使用。録音は恒久保存せず、OSの音声サービスで外部処理される可能性を録音前に説明する。分析SDK、広告、学習履歴の外部送信はv1に入れない。クラウド同期やタスク連携を動いているように表示しない。
