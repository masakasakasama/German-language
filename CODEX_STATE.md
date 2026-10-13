# CODEX_STATE

Updated at: 2026-10-13 (Asia/Tokyo)
Status: Android v3 (0.1.3) published with 28 interactive grammar diagrams and 28 boxed examples; public APK/ZIP/metadata verified; original signing key retained.
Goal: ReiseDeutsch, a German-only Android learning app based on the approved design, existing SRS behavior, and reviewed cross-app design references.

## Delivered in the repository

- Foundations course: 14 units, 28 explanatory sections, 28 decomposed examples, 42 rationale-backed questions. Retry incorrect answers before completing course exercises. Course cards reuse existing SRS, progress, collection and backup; no DB schema change.
- Startup update checks with optional setting; user confirms APK download/install. Fixed signing flags for Android 9–12 in v2.
- Native Android Compose app with playful ReiseDeutsch screens, map, lessons, conversations, flashcards, collection, settings and backup.
- 5 cities, 20 lessons, 60 phrases, 30 vocabulary entries, 12 conversations.
- Ported SRS and answer grading, local Room persistence, progress/XP/streaks, language speech tools, JSON backup/restore and verified APK in-app update flow.
- GitHub Actions verification/release workflow and pinned Tatsu design-system module.
- Product/design/release/acceptance docs and reference artwork.

## Validation

- `./gradlew testDebugUnitTest lintDebug assembleDebug --no-daemon`: passed 2026-10-12.
- `./gradlew testDebugUnitTest lintRelease assembleRelease --no-daemon`: passed; release minification works. A production APK signed with the generated persistent release key passed apksigner verification.
- Unit tests cover scheduler spacing, Again behavior, weak-card scoring and answer normalization.
- Debug APK generated at `app/build/outputs/apk/debug/app-debug.apk` (17,709,330 bytes).
- Android emulator/device is unavailable in the cloud environment; UI, speech permission/recognition and OS installer have not been device-tested.
- Source commit `00af5f587c8ad2cf419937551d2fdffec373534d` is pushed to `main`. Release v1 is public at https://github.com/masakasakasama/German-language/releases/tag/v1 with the signed APK and latest.json.
- GitHub Actions run `38322642050` on the `release-apk` branch successfully published the prebuilt signed APK using the automatic repository token. No Codex environment changes or personal token are needed for this release.
- Downloaded both public assets, verified APK signature and matching metadata size/checksum: 1,254,740 bytes; SHA-256 `9739506516e92a04811fb17a13ffad2830dedea1930b9a6f0c159d065aa5bd23`.

- v2 source commit `7312329561ec8cf6442ee1c9b0c926658658188a`; release https://github.com/masakasakasama/German-language/releases/tag/v2.
- v2 local checks `testDebugUnitTest lintRelease assembleRelease` passed (9 JVM tests). Publisher run `38324772955` succeeded on retry after the v2 tag/draft was created with the existing integration.
- v2 public release/direct downloads and latest.json verified: 1,287,508 bytes; SHA-256 `201aab3088587cef2251056cac9f8bd78c41fb60f11e6988ff2b01088317ba82`. Signing certificate matches v1: `f2ff2596fd6555775a537810658021b56fedfc3cbbbf64c84f2c73f89e76dbf2`.
- CI SDK setup previously failed trying to install the obsolete package `tools`; updated packages to platform-tools, platforms;android-35 and build-tools;35.0.0. Main verification run `38324587832` passed SDK setup, tests, lint and debug APK build.

## Current blockers

- The current GitHub API identity can push code but cannot manage Actions secrets (403). Signing secrets are not set; the long-lived key and credentials are stored in ignored, owner-only `.local-release/` files in the workspace.
- The unapplied Codex credential/network draft is unnecessary for the published initial release; do not request a personal GitHub token for downloading it.
- 2026-10-13 Notion retry: URL and content endpoints are HTTP 200, but content records are empty and public page metadata reports `publicAccessRole: none`. The specific page is inaccessible without authenticated access; common Notion principles/update process remain unverified. Need published link, authenticated connector, or supplied text. Network 403 is no longer the current diagnosis.

## Continue

1. Initial release upload and public asset/checksum verification are complete. Download APK from the public v2 Release or the direct link documented in README for installation.
2. Securely register `.local-release/` signing values as repository Actions secrets for later signed workflow releases.
3. Install/test the APK on a real Android device, including the update flow.
4. Revisit Notion-backed common design/update guidance once page access is available.

## ZIP download fallback (2026-10-13)

User reports APK downloads do not finish on phone. Added `distribution/ReiseDeutsch.zip` on release-apk commit `231865a2a889e6bbaff77062e4a8fedfe43a7db2`. Public ZIP is 1,095,939 bytes and contains only the exact signed 0.1.2 APK. Downloaded public ZIP and verified archive integrity and extracted APK SHA-256. Device-specific cause remains undiagnosed; do not claim the alternate format is confirmed working on the phone. A separate publisher adds the ZIP to Release v2 without replacing APK or latest.json.

## Diagram course release v3 (2026-10-13)

- Source commit `bf5c6eea415fd661d31f528dd9d8cfde6aadd269`. All 14 units have two diagrams, 28 total. Roles are explicitly authored; learners can emphasize a role through filters. Word-box wrapping supports narrow screens; role labels accompany colors. All 28 examples now show their decomposed phrases as boxes.
- Existing lesson/card IDs and database schema are unchanged. Tests (9), debug lint/build, release lint/build passed. Main CI run `38341198442` passed.
- Public release v3: https://github.com/masakasakasama/German-language/releases/tag/v3. Publishers `38341516847` (APK/feed) and `38341516837` (ZIP) passed.
- APK: 1,303,892 bytes, SHA-256 `334407d19528913707d6a0410f7a199f0e01eda9e8865fc44e4ee9cfb7d9f782`. Public APK signature matches the original signing certificate. Public ZIP download (1,117,961 bytes) extracted and verified against the same hash. GitHub latest redirects to v3.
- APK and ZIP direct links use distribution commit `a07a2fef1b995044a1f9a7bbccc66560525ab838`; ZIP is versioned `ReiseDeutsch-0.1.3.zip` to distinguish it from earlier downloads. Real-device rendering, installer behavior and resolution of the user’s phone download issue remain unverified. Notion page access remains restricted as documented above.
