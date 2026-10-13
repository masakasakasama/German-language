# ReiseDeutsch

旅するように、ドイツ語を。日本語話者向けのAndroid専用ドイツ語学習アプリです。ユーザー指定の旅行うさぎ・水彩ドイツ風景のデザインに合わせ、既存アプリの自己評価と間隔反復ロジックを取り入れています。

## 動作確認

- 5都市・20旅行レッスン・60旅行フレーズ・30語彙・12会話を同梱
- 日本語でしくみを説明する基礎コース14課・42問：音と文字、語順、動詞、冠詞、格、疑問文、否定、助動詞、分離動詞、前置詞、完了形、weil
- 0.1.3では全14課に計28の図解を追加。主語・動詞・対格・与格などを色と日本語ラベルで区別し、選んだ役割を強調。28例文も意味ごとの箱に分解
- 各課に文の分解、理由付き解説、誤答の再挑戦。42枚の基礎カードも既存のSRS・コレクション・バックアップで管理
- 学習カードの自己評価、復習スケジュール、選択/入力問題、会話、読み上げ、音声認識
- 端末内の学習記録、進捗、連続日数、XP、JSONバックアップ/復元
- 日本語表示切替、Light/Dark、学習目標、アプリ内更新確認
- 0.1.2以降は起動時に新しい版を自動確認して案内（設定でOFF可）。ダウンロードとOSによるインストールは利用者の確認で実行
- 0.1.4では全画面の戻るジェスチャー/戻るボタンを処理。ホームでは終了を確認し、学習中に戻っても入力・選択・問題の状態を保持して再開

Android SDK 35とJava 17以上が必要です。

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`（約17 MB）。debugアプリは本番版とは異なるapplication IDと開発用署名を使います。

## 配布

[ReiseDeutsch 0.1.4 APKをダウンロード](https://github.com/masakasakasama/German-language/releases/download/v4/ReiseDeutsch-0.1.4.apk)（Android 8.0以上、約1.2 MB）。[公開リリース](https://github.com/masakasakasama/German-language/releases/tag/v4)にはアプリ内更新用の `latest.json` も添付しています。公開後にAPKをダウンロードし直し、署名・サイズ・SHA-256を検証済みです。実機での動作確認は未実施です。

0.1.1〜0.1.4はローカルで署名・検証したAPKを `release-apk` ブランチの専用GitHub Actionsで公開しました。GitHub Actionsの自動トークンを使用するため、ダウンロードにCodex環境設定や個人トークン登録は不要です。初回版と同じ署名で、既存の学習データを残して上書き更新できます。

main push時はテスト・lint・debugビルドを実行します。将来の署名付きビルド・公開はmainに対するworkflow_dispatchで実行し、固定署名鍵のGitHub Actions Secrets (`REISE_KEYSTORE_BASE64`, `REISE_KEYSTORE_PASSWORD`, `REISE_KEY_ALIAS`, `REISE_KEY_PASSWORD`) が必要です。既存インストールを更新するため、初回版と同じ署名鍵を使用してください。

GitHub Releaseからのダウンロードが止まる端末向け：[0.1.4の直接ダウンロード](https://raw.githubusercontent.com/masakasakasama/German-language/1e14ec0c98cf929c449f404f655b61a4009ace33/distribution/ReiseDeutsch-0.1.4.apk)。0.1.1のAndroid 9〜12ではアプリ内更新の署名取得に不具合があるため、このリンクから更新してください。旧版のアンインストールは不要です。

APKのダウンロードが完了しない端末向け：[ZIP形式でダウンロード](https://raw.githubusercontent.com/masakasakasama/German-language/1e14ec0c98cf929c449f404f655b61a4009ace33/distribution/ReiseDeutsch-0.1.4.zip)。スマホの「ファイル」または「マイファイル」→「ダウンロード」でZIPを展開し、中の `ReiseDeutsch-0.1.4.apk` を開いてください。公開済みAPKと同一のファイルであることを、公開ZIPの展開とSHA-256照合で確認済みです。

## 設計資料

[基礎コースの内容と更新の動作](docs/design/foundations-course.md)、[戻るジェスチャーの修正](docs/design/back-navigation.md)。仕様・根拠・画面設計は [docs/design](docs/design/) にあります。Notion共通設計の指定ページはHTTP 403で取得できていないため、本文準拠を主張せず、確認できた既存GitHub実装を基にしています。
