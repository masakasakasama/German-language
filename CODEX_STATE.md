# CODEX_STATE

Updated at: 2026-10-12 (Asia/Tokyo)
Status: Android v1 implemented, signed, published on GitHub Releases, and public download verified; source is on main.
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
- Source commit `00af5f587c8ad2cf419937551d2fdffec373534d` is pushed to `main`. Release v1 is public at https://github.com/masakasakasama/German-language/releases/tag/v1 with the signed APK and latest.json.
- GitHub Actions run `38322642050` on the `release-apk` branch successfully published the prebuilt signed APK using the automatic repository token. No Codex environment changes or personal token are needed for this release.
- Downloaded both public assets, verified APK signature and matching metadata size/checksum: 1,254,740 bytes; SHA-256 `9739506516e92a04811fb17a13ffad2830dedea1930b9a6f0c159d065aa5bd23`.

## Current blockers

- The current GitHub API identity can push code but cannot manage Actions secrets (403). Signing secrets are not set; the long-lived key and credentials are stored in ignored, owner-only `.local-release/` files in the workspace.
- The unapplied Codex credential/network draft is unnecessary for the published initial release; do not request a personal GitHub token for downloading it.
- The requested Notion page returned HTTP 403; common Notion principles and update process remain unverified.

## Continue

1. Initial release upload and public asset/checksum verification are complete. Download APK from the public v1 Release for installation.
2. Securely register `.local-release/` signing values as repository Actions secrets for later signed workflow releases.
3. Install/test the APK on a real Android device, including the update flow.
4. Revisit Notion-backed common design/update guidance once page access is available.
