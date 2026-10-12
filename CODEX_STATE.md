# CODEX_STATE

Updated at: 2026-10-12 (Asia/Tokyo)
Status: Android v1 implemented, signed and locally verified; source is on main; release asset upload awaits cloud egress permission.
Goal: ReiseDeutsch, a German-only Android learning app based on the approved design, existing SRS behavior, and reviewed cross-app design references.

## Delivered in the repository

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
- Source commit `00af5f587c8ad2cf419937551d2fdffec373534d` is pushed to `main`. The APK and checksum feed are prepared locally. GitHub rejected the release asset upload at `uploads.github.com`; `v1` is not yet present in the release list.

## Current blockers

- The current GitHub API identity can push code but cannot manage Actions secrets (403). Signing secrets are not set; the long-lived key and credentials are stored in ignored, owner-only `.local-release/` files in the workspace.
- Environment configuration draft adds `uploads.github.com`; it must be reviewed/published before the APK and feed can be uploaded.
- The requested Notion page returned HTTP 403; common Notion principles and update process remain unverified.

## Continue

1. After the environment config is published, upload the signed APK and `latest.json` to GitHub Release v1; verify the public assets and checksum.
2. Securely register `.local-release/` signing values as repository Actions secrets for later signed workflow releases.
3. Install/test the APK on a real Android device, including the update flow.
4. Revisit Notion-backed common design/update guidance once page access is available.
