# アプリ更新・配布設計

## 根拠と決定

GitHub確認済み: HomeはGitHub Releasesから最新版を確認し、APKダウンロード→OSインストールへ進む。versionCodeはGitHub Actionsのworkflow `run_number`（repo内で単調増加）を使用。SEN公開feedはversionCode/versionName/apkUrl/sha256を持つ。共通Design_system更新はrevision固定→ビルド/テスト→PR→人が画面差分を確認→配信。

Notionの共通設計・更新方法はURLを受領済みだがHTTP403で本文未取得。この設計は**GitHubの確認済み方式を基にしたReiseDeutschの具体案**であり、「Notionに従った」とは称さない。Lunaは更新機能まで実装可能だが、公開前にNotionとの照合を行う。個別APK配信先をSENのOracleサーバーへ勝手に追加しない。

v1は `masakasakasama/German-language` の公開GitHub ReleasesでAPK＋latest.jsonを配布する。別の配布repoや独自サーバーは作らない。GitHub連携の読取成功はActions実行権限・公開repo設定・署名Secrets・書込権限の証明ではない。公開状態を確認する必要がある。

## バージョンと署名

本番パッケージ `com.masakasakasama.reisedeutsch` を固定。v1はrelease用の固定署名証明書を継続使用。署名鍵とパスワードはGitHub Actions Secretsに置き、コード・ログ・バックアップ・APK metadataに載せない。debug用鍵/既存Appの固定鍵を製品鍵として流用しない。鍵は初回配布前に確保し、署名がない場合はdebug検証まで進め、release済みと報告しない。

バージョンはGitHub Actionsの `run_number` をversionCodeとして使う。repo内で単調増加する番号を用い、release concurrencyで同時公開を防ぐ。versionName="0.1.<versionCode>"、tag="v<versionCode>"、APK名="ReiseDeutsch-<versionName>.apk"。同じ版の再発行・別署名への置換は禁止。未配布debugの版数は本番feedと比較しない。

## 配布パイプライン

PRではunit test、教材validator、lint、debug build、UI/screenshot確認。mainへ反映後のreleaseは同じソースSHAをfull checkout→版数確認→固定鍵でrelease build→apksigner verify→package/version/certificate確認→SHA-256/ファイルサイズ計算→GitHub draft releaseへAPKとfeedを添付→検証→公開。feedはアップロード済みAPKの実URLを指す。失敗時は新releaseを公開しない。最初の配布で前版からの更新試験が不可能な場合は、同じ鍵で作った検証版→初版の更新を試す。

実装済みworkflowはPR/main pushでtest/lint/debug buildを行い、手動実行（workflow_dispatch）ではActions secretsを使って署名releaseを作る。手動release前に`REISE_KEYSTORE_BASE64`, `REISE_KEYSTORE_PASSWORD`, `REISE_KEY_ALIAS`, `REISE_KEY_PASSWORD`を登録する。署名Secretsのない通常pushでrelease jobを失敗させない。GitHub認証のread accessはwrite accessの証明ではない。

## 更新フィード契約

latest.jsonはreleaseのasset。必須フィールド:

| field | 型・条件 |
|---|---|
| schemaVersion | 1 |
| packageName | com.masakasakasama.reisedeutsch |
| versionCode | 正整数。インストール版より大きい場合だけ更新候補 |
| versionName | 表示名0.1.N |
| minSdk | 26（更新版が変更する場合は端末適合性を確認） |
| sourceCommit | ビルドした完全な40文字SHA |
| releasePageUrl | 自repoのhttps://github.com/.../releases/tag/vN |
| apkUrl | 自repoのhttps://github.com/.../releases/download/vN/ReiseDeutsch-0.1.N.apk |
| sha256 | 実APKの64桁hex |
| sizeBytes | 実APKの正のサイズ |
| notes | 日本語の変更点。最大4000文字、HTMLを実行しない |

署名証明書は実行中APKの署名と比較する。feedに書かれた任意の証明書を信頼の根拠にしない。未知schema、package不一致、URL/path不一致、欠落、minSdk非対応は候補を拒否する。通常の更新は任意で、強制更新機構はv1に入れない。

最新版はHome同様に `/releases/latest` の公開redirectからtagを取得する。自repoのtag/pathのみ許可し、`/releases/download/<tag>/latest.json` を読む。GitHub REST APIや利用者のGitHub tokenは不要。feed取得はHTTPS、タイムアウト15秒、最大64KB。公開assetへのredirectはGitHubの配信ホストだけを許可し、ユーザーから任意ホストを設定できるようにしない。

## 利用者の更新フロー

起動時は前回成功確認から24時間以上なら非同期確認、設定には「今すぐ確認」。学習の表示や回答を待たせない。候補があればホームの控えめな更新カードと設定に表示する。カードは版名・変更点・容量・「更新する」「あとで」。

更新する→アプリ専用cacheへダウンロード→サイズ/SHA-256/package/version/署名一致検証→必要なら不明なアプリのインストール許可画面→FileProviderのcontent URIでOSインストーラ起動。無断・無確認のインストールはしない。APKは検証完了前にインストーラへ渡さない。

取得中は進捗と中止。Content-Lengthがない場合は不確定進捗。ネット切断は再試行可能、Range再開はETagとサーバー応答を確認できる場合のみ。不一致なら部分ファイルを消して取り直す。通常学習記録は更新キャッシュと分離する。

| 更新状態 | 表示と動作 |
|---|---|
| Checking | 確認中。学習は利用可能 |
| UpToDate | 最新版です、確認日時 |
| Available | 版名、変更点、容量、更新する/あとで |
| Downloading | 進捗、キャンセル |
| Verifying | 検証中。インストール不可 |
| PermissionRequired | OS設定を開く、戻って再開 |
| InstallerOpened | OSへ引継ぎ。成功とは扱わない |
| Failed | 通信/破損/別package/別署名/非対応を区別、再試行 |

OSから戻っただけで更新済みにしない。次回起動のBuildConfig.VERSION_CODEが候補版と一致/上回ることで判定。インストールをキャンセルしても学習記録と候補は残す。成功時のみ一時APKを掃除するか、7日経過のキャッシュを削除。debug版は更新インストールを無効にし、説明を出す。

## 更新で守るデータ

上書きインストールでRoom/DataStoreを保持。Room migration、教材ID mapping、旧backup復元、進行中sessionを検証する。破壊的migrationや更新のためのアンインストールは許容しない。緊急修正は新しいversionCodeで修正版を公開し、古いAPKへのダウングレードで復旧しない。

## 共通デザイン更新

pinは `reference/sources.json` のcommit。新revisionは専用branchでvendor生成物とhashを更新し、ホーム/会話/地図のLight/Dark比較、build/testを行う。素材の個性と共通tokenの変更を別に説明する。共有repoへの直接push、実行時remote tokenの取得、自動mergeは採用しない。Homeへの新Appタイル追加は別repoへの変更なので、本Appの初版完成条件には含めず必要時に別途実施する。

## Candidate signing without local Android SDK (v6)

If the temporary SDK/toolchain is absent, `.github/workflows/build-candidate.yml` builds the unsigned release on GitHub Actions (currently BUILD_NUMBER=6; change deliberately for a new release). It runs tests/lint and packages the unsigned APK, exact source SHA, test results and the official SDK's apksigner.jar. Existing fixed signing key stays local under ignored `.local-release/`; never add it to artifacts or Git.

Download the candidate via `gh run download`. In the current environment the artifact storage host returned Forbidden. For v6, `.github/workflows/stage-candidate-v6.yml` used Actions' artifact download to attach this same candidate bundle to the precreated **draft** v6 release. Downloaded it via `gh release download`, verified source/version/accepted artwork, signed locally with the original key using environment-based password arguments to apksigner.jar, verified certificate, then removed the unsigned draft asset **before** publishing the final signed APK. No personal token or environment settings change was required. This staging workflow is pinned to the v6 source/run and is not a general-purpose publisher.

The existing release-apk publisher verifies signed APK hash/size/feed and publishes APK plus latest.json, with a separate ZIP publisher. Never publish the unsigned staging bundle as a user release. Main source tag and release feed source SHA must identify the built source, not a later docs/distribution commit.
