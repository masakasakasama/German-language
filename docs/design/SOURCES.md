# 根拠と未確認事項

## 仕様の優先順位

1. ユーザーの明示指示（Android専用、既存学習ロジック流用、正式参考画像、設計担当とLuna実装の分担）。
2. 本設計で確定した製品の振る舞い。
3. 確認済みの既存コードと共通設計資料。矛盾がある場合は変更を明示する。
4. Notionの共通方針。現在は本文未取得で、内容を推測して確定事項にしない。取得後に明示指示との整合を確認する。

以前の会話で挙げた「Kotlin」「Room」等は提案だったが、本設計で具体的な仕様として採用した。Lunaは以前の提案を別案として再設計し直す必要はない。

## 確認したソース

参照ごとの正確なcommitとファイルSHA-256、immutable URLは [reference/sources.json](reference/sources.json) に保存した。

| repo | 確認内容 | 流用/採用 |
|---|---|---|
| Language_learning | srs.js、storage.js、views.js、exercises.js、data/de.js、styles.css | 3段階自己評価、復習、苦手判定、ドイツ語教材、学習構造 |
| CPRE / CAPM_Baby | app/progress.ts | 正誤×自信の別ロジック。今回のSRSとは混ぜない |
| Design_system | tokens、README、ARCHITECTURE、ADOPTION、AUTOMATION、Android Theme | ビルド時配布、revision固定、semantic token、共通部品、更新レビュー |
| Home | README、GitHubReleaseClient | GitHub Releasesの最新版確認・count版数・OSインストール導線 |
| sen-android-updates | README、latest.json | 更新metadataの版数/APK URL/hashの構造。配信サーバーは流用しない |
| AAOS_study | Android build設定 | minSdk26と既存Android構成を参照。旧toolchainを無検証コピーしない |
| Brain | README、AGENTS | GitHubを永続状態とし、実装結果/Next/Blockers/Verificationを保存する運用 |

参照ソースはローカル `/tmp/german-reference-*` に取得して調べた。将来の環境にそのパスが残ることは前提にしない。Lunaはsources.jsonのcommitから必要なソースを取得する。SRSの原文と互換fixtureは本repoに保持する。

## 指定画像

ユーザー添付21090.jpgを原形のまま `reference/reisedeutsch.jpg` に保存した。ホーム・会話・地図・旅行うさぎ・水彩背景・クリームカード・配色の基準。個別素材/画像内のUIテキストを完成済みの素材とは扱わない。

## Notion

URL: https://app.notion.com/p/Tatsu-Profile-363290827880811d91b6ca4d9b057b1d?source=copy_link

2026-10-12にこのURLとNotionの標準ページURLを試行し、403で取得できなかった。接続用ドメインapp.notion.com/notion.so/www.notion.soを環境設定draftへ保存したが、設定公開・実行環境への反映・ページ閲覧権限は別の条件。後の再試行もHTTP403。ネットワーク許可後の公開ページ本文/Notion連携/本文エクスポートのいずれかで確認する必要がある。403だけからページが非公開だとは断定しない。

2026-10-13に再取得。指定URLのHTMLはHTTP 200で到達できたが、本文データは空。Notionの公開ページ確認は `publicAccessRole: none` を返した。現在の阻害要因はログインなしでのページ閲覧権限で、ネットワーク403とは区別する。公開されたNotionリンク、認証済みNotion連携、またはユーザーが提供する本文が必要。

「共通設計思想」「アプリ更新方法」の原文・子ページは未確認。GitHub資料をNotionの原文として引用していない。Lunaのローカル実装をこの未確認で止めない。公開配信前に更新/署名/保存/共通デザインの相違を確認し、相違があればRELEASE/ARCHITECTUREを改訂する。

## 仕様として意図した既存との差

- introの二重SRS更新を修正。
- 同一イベントの評価変更は置換して累積更新しない。
- reps=0にリセットされた既学習の苦手カードも抽出する。
- 期限の暦日はLocalDateとして保存し、夏時間とzone変更の動作を明示。
- XP/日次目標の重複加算を防ぐ。
- 本番APKは固定のrelease署名を使い、更新時にhash/package/署名を検証。
- ドイツ語専用、日本語UI、単一ローカルプロファイル。Web版の複数言語/複数ユーザー/同期/連携はv1から外す。

## 設計時の確認と製品検証の区別

原JSの代表復習遷移は実行確認済み。これはAndroid移植の完成を意味しない。Androidコード、個別アート、音声素材、実機受入、署名release、公開はLunaの実装/配信工程に残る。
