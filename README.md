# ReiseDeutsch

旅するように、ドイツ語を。日本語話者向けのAndroid専用ドイツ語学習アプリです。ユーザー指定の旅行うさぎ・水彩ドイツ風景のデザインに合わせ、既存アプリの自己評価と間隔反復ロジックを取り入れています。

## 動作確認

- 5都市・20レッスン・60フレーズ・30語彙・12会話を同梱
- 学習カードの自己評価、復習スケジュール、選択/入力問題、会話、読み上げ、音声認識
- 端末内の学習記録、進捗、連続日数、XP、JSONバックアップ/復元
- 日本語表示切替、Light/Dark、学習目標、アプリ内更新確認

Android SDK 35とJava 17以上が必要です。

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`（約17 MB）。debugアプリは本番版とは異なるapplication IDと開発用署名を使います。

## 配布

GitHub Actionsのrelease workflowはmain push時にテスト、lint、release APK署名、checksum付き `latest.json` 生成、GitHub Release公開を行う構成です。公開には固定署名鍵をGitHub Actions Secrets (`REISE_KEYSTORE_BASE64`, `REISE_KEYSTORE_PASSWORD`, `REISE_KEY_ALIAS`, `REISE_KEY_PASSWORD`) に設定する必要があります。初回ユーザー配布リリースは署名鍵とGitHub書き込み認証が設定されるまで未公開です。

## 設計資料

仕様・根拠・画面設計は [docs/design](docs/design/) にあります。Notion共通設計の指定ページはHTTP 403で取得できていないため、本文準拠を主張せず、確認できた既存GitHub実装を基にしています。
