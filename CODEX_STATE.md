# CODEX_STATE

Updated at: 2026-10-12 (Asia/Tokyo)
Status: Android v1 implemented and locally verified; public signed release blocked on GitHub write/signing setup.
Goal: ReiseDeutsch, a German-only Android learning app based on the approved design, existing SRS behavior, and reviewed cross-app design references.

## Delivered in the repository

- Native Android Compose app with playful ReiseDeutsch screens, map, lessons, conversations, flashcards, collection, settings and backup.
- 5 cities, 20 lessons, 60 phrases, 30 vocabulary entries, 12 conversations.
- Ported SRS and answer grading, local Room persistence, progress/XP/streaks, language speech tools, JSON backup/restore and verified APK in-app update flow.
- GitHub Actions verification/release workflow and pinned Tatsu design-system module.
- Product/design/release/acceptance docs and reference artwork.

## Validation

- `./gradlew testDebugUnitTest lintDebug assembleDebug --no-daemon`: passed 2026-10-12.
- `./gradlew testDebugUnitTest lintRelease assembleRelease --no-daemon`: passed; release minification works. Gradle produced only an unsigned APK because no production key is configured.
- Unit tests cover scheduler spacing, Again behavior, weak-card scoring and answer normalization.
- Debug APK generated at `app/build/outputs/apk/debug/app-debug.apk` (17,709,330 bytes).
- Android emulator/device is unavailable in the cloud environment; UI, speech permission/recognition and OS installer have not been device-tested.
- GitHub remote read and dry-run were checked. No commit has been pushed or public release created.

## Current blockers

- `gh auth status` reports the configured GH_TOKEN is invalid. Existing public remote read access works; no push/API write permission has been verified.
- GitHub Actions signing secrets are not set. A stable production signing key must be provisioned before a user-facing release.
- The requested Notion page returned HTTP 403; common Notion principles and update process remain unverified.

## Continue

1. Establish authenticated GitHub write access for `masakasakasama/German-language`.
2. Securely provision the long-lived Android upload/signing key and repository Actions secrets.
3. Commit and push the verified source; wait for signed release workflow; confirm release APK, metadata, update URL and checksum.
4. Install/test the APK on a real Android device, including the update flow.
5. Revisit Notion-backed common design/update guidance once page access is available.
