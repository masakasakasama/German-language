# CODEX_STATE

Updated at: 2026-10-13 (Asia/Tokyo)
Status: Android v4 (0.1.4) published with system Back handling on every screen, Home exit confirmation and paused study state retention. Public APK/ZIP/feed verified; original signing key retained.
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

## Back gesture fix v4 (2026-10-13)

- Source `a019de01da63c9ae1b43f66a73aa55d0e314fa58`. v3 registered BackHandler only on three course pages, so other pages fell through to Activity exit. v4 uses one BackHandler for all screens and NavigationHistory with course/conversation IDs. Home requests explicit exit confirmation; dialogs dismiss first.
- Paused study retains typed text, input mode, selection, revealed/answered state; lesson entry resumes the active lesson. Choice order is seeded by session ID/step. Completion replaces the study history entry. Session IDs are persisted consistently and session writes serialized; only one active session is stored and old completion does not delete a newer session.
- 15 JVM tests pass, including 6 navigation regression tests. Debug tests/lint/build and release tests/lint/build pass. Main CI `38343045521` passed. Manifest BackInvoked flag is present and true in APK.
- Public v4 release: https://github.com/masakasakasama/German-language/releases/tag/v4. Publishers `38343398154` (APK/feed) and `38343398135` (ZIP) passed.
- APK size 1,320,316 bytes; SHA-256 `f0ccf1ec69c644c0a0374ec683652ee7d90b3cf981cff1e27ba167dc23a846c9`. Signing certificate matches prior versions. ZIP size 1,121,040 bytes; public ZIP downloaded, extracted and checked. Public release APK/feed downloaded and checked; latest resolves to v4. Direct distribution commit `1e14ec0c98cf929c449f404f655b61a4009ace33`.
- Actual edge gesture, dialog behavior and navigation on a physical device remain untested. Navigation regression tests verify application history logic, not Android gesture rendering.

## Animated 3D bunny release v5 (2026-10-14)

- Source `797252cd87fcca7cbb6f23a59d4e449b57fb4a92`, distribution `ea63c83232ae45ed8dde1f865271e5bd98775996`. Public release https://github.com/masakasakasama/German-language/releases/tag/v5. Both publishers passed: APK/data `38570713111`, ZIP `38570713121`.
- Created original stylized travel bunny from existing character/reference: cream rabbit, red beret, navy coat, backpack, suitcase. 61 parts, 32,208 triangles. Editable `art/mascot/reise-bunny.blend`, portable `reise-bunny.glb` with Idle/Wave node animation tracks, source generator, data ZIP, actual mesh/shader preview images. It is an object hierarchy rig, not a skinned skeleton or VRM avatar.
- Native `Bunny3D.kt` on Home loads local mesh/material data generated from the same Blender objects; idle bob and shoulder-pivot wave, pause button, approximately 30 fps, lifecycle pause, respects disabled system animations, fallback to 2D on renderer initialization exception. No network/model service dependency. No database or learning changes.
- Debug tests/lint/build and release tests/lint/build passed; 15 unit tests. Mesh validator passed; Khronos glTF Validator: 0 errors, 0 warnings (61 informational unused UV attributes). Offscreen Mesa GLES2 compiled the actual shader and rendered actual mesh; idle/wave frames differ. No Android emulator/device rendering/performance/scroll composition test was available. Main hosted verification `38570270134` was still running at this documentation update; do not claim it passed without checking.
- APK 1,716,899 bytes; SHA256 `8116a6d6d1640975db5b70e43473587f3a32d27164a0349428666107303c90f1`. Public APK/feed downloaded and verified, latest redirects to v5; signing certificate matches v1-v4. Public raw ZIP 1,457,645 bytes downloaded/extracted/hash verified. User's specific phone download behavior remains unverified.
- Data ZIP 618,578 bytes; SHA256 `8ac8c08d2f4b6014015b56ed0e06f3ec602b2ff451f2a48732bdab743b08f005`; GLB 738,456 bytes. Both public raw downloads verified. Source ZIP link pinned to source commit; APK ZIP link pinned to distribution commit.
- Blender 4.3.2 was already installed. Optional host render smoke tool uses PyOpenGL (installed this turn), numpy, Pillow and Mesa EGL. Existing Android setup unchanged; no environment config changes required for Android build.

## Softer mascot revision (2026-10-14, preview only)

User explicitly rejected v5's mascot as not cute and frightening. Reworked the actual 3D geometry: oversized round face, shortened body/arms, removed protruding muzzle and round dark mouth, small drawn smile, flush cheek patches, softer pastel palette and flatter ambient lighting. Regenerated Blender, GLB, Android buffers and bundle; 99 parts / 51,280 triangles. Updated wave shoulder pivot for shorter body. Mesh validation and actual GLES2 idle/wave smoke render pass. Present `art/mascot/runtime-idle.png` as the revised appearance, not a device screenshot. This revision has NOT been published as an APK; v5 still contains the old mascot. Do not send the old v5 APK claiming it contains this fix.
- Revised Android debug build passed (`assembleDebug`, 52 seconds). Production release and device test have not been performed for this revision.

## Material realism revision (preview v3)

User asked for more realism after the softer face revision. Asked whether plush/material realism or anatomically realistic rabbit was intended; no answer arrived before independent work completed, so proceeded with the stated plush/material direction. Added short geometric head fibers, woven cap/coat, leather case and sewn seams; luggage is now a rounded box. 163 parts / 88,501 triangles. Deterministic 256px color and normal maps are embedded in the GLB; native GLES2 now uses UVs/color textures with diffuse/rim shading. Native rendering does not implement Blender's full PBR/normal-map appearance. Mesh binary is format 2, 32-byte XYZ/normal/UV vertices.

- Mesh integrity and actual GLES2 textured idle/wave rendering pass. Khronos glTF Validator returns zero errors and zero warnings after exporting tangents and using an untextured fiber material. `preview.png` is the Blender studio render; `runtime-*.png` are native-shader offscreen renders. Neither is an Android device screenshot.
- Source commit `5d26b52daeaea7dac4432463f616b7aa615ca400`. Hosted Android verification run `38641836659` pending at documentation time. Previous temporary Android SDK/JDK paths are absent in the current shell; system Java 21 is present. Reused repository GitHub Actions for Android build checks instead of assuming previous local setup persisted. The preceding softer model revision CI `38575144435` passed.
- Updated editable Blender/GLB and data bundle include `tools/mascot_materials.py` as well as the generator. No new production APK/release; v5 still contains the first model. Real device rendering and performance are unverified.
- Hosted Android run `38641836659` completed successfully (verify job 4m24s): unit tests, lintDebug and assembleDebug all passed. Source `5d26b52daeaea7dac4432463f616b7aa615ca400`; final preview/data package commit `661035c6b3587f2aa6c217b6158ec2af3c57e691`. Public preview PNG and 4,619,069-byte data ZIP downloaded and verified byte-for-byte; ZIP integrity passed. Production APK remains unchanged.

## Accepted artwork animated and released v6 (2026-10-15)

- User rejected prior 3D primitives and asked to preserve the original reference. Generated realistic artwork approved by "これでうごかして". Original accepted image preserved at `art/mascot/accepted-reference.png`; transparent cutout at `app/src/main/res/drawable-nodpi/travel_companion.png` (1374×1145, real alpha, SHA256 `f327c140d8cc0a3dad512808a3f755d7e1c912f274b99b9a77fddf3e3caa14fd`). This is image-based animation, NOT a new 3D mesh. Do not describe it as articulated walking, hand waving or an exact animated 3D reconstruction.
- `AnimatedTravelCompanion.kt` replaces `Bunny3DCard` on Home. Whole sprite gently floats/tilts, taps or "いっしょに出発！" trigger a two-stage bounce. Pause toggle, lifecycle pause/disposal and system animation-disable support. Arms/luggage remain attached because the entire image moves together. Old 3D code/assets remain archived/unreferenced by Home; no schema or learning changes.
- Source/tag v6: `ae3711f0da355ba434f7402c4c63dcccd48c28a6`. Android verify `38725228857` and candidate tests/lintRelease/assembleRelease `38725228789` passed; 15 JVM tests, no failures. No emulator/physical-device UI or motion test was performed.
- Local temporary Android toolchain was absent. Built unsigned candidate on Actions with official apksigner.jar included. Artifact direct download hit Forbidden on artifact storage host; reused draft GitHub Release staging workflow `38726009330` to transfer the candidate. Fixed original local key signed it; certificate matches v1-v5. Removed unsigned draft bundle before publishing. No user token or environment settings needed. Reusable procedure in docs/design/RELEASE.md.
- Distribution commit `4ea4dfb1be374488405bebb89ce412df4924758c`. Public release https://github.com/masakasakasama/German-language/releases/tag/v6. Publishers `38726203550` and `38726203711` both passed. Only signed APK, ZIP, latest.json are public assets.
- APK size 4,514,289 bytes, SHA256 `7034ba05a9bd6f5793a50bd904a647a44aa972603d8a823e819be1368dfa36ef`. Public raw ZIP size 4,055,647 bytes; downloaded/extracted and matched signed APK. Public APK/feed downloaded and checked; latest resolves to v6. Verified package/versionCode6/versionName0.1.6 and exact visible artwork pixels/alpha inside APK (AAPT clears hidden RGB of fully transparent pixels, which is expected).
- Signing certificate SHA256 remains `f2ff2596fd6555775a537810658021b56fedfc3cbbbf64c84f2c73f89e76dbf2`. Existing install can be updated without uninstall. Phone-specific download/installer behavior remains unverified.

## Unified character and explicit playback fix v7 (2026-10-15)

- User reported no movement and asked to replace every character. v6 confirmed code issue: when `ValueAnimator.areAnimatorsEnabled()` was false, both animation and playback controls were disabled; idle motion was also subtle (3dp/.8 degrees). The user's exact device condition was not observed, so do not claim this was conclusively their device's cause.
- New shared `CompanionCharacter` uses the accepted transparent sprite everywhere. TravelBunny now delegates to it, replacing vector art in Home headers, courses, conversations, profile/settings/backup and other BunnyCards. Empty states, completion screen and current map marker replaced; decorative rabbit emoji removed from text/toasts. Adaptive launcher and round icons use the same image. Old Bunny3DCard definition/assets remain unused; no call sites. No learning/schema changes.
- Explicit frame-clock playback uses `withFrameNanos` and pure `companionPose`, independent of animator duration scaling after user-initiated playback. Auto motion respects system disabled animations. Both main playback controls remain enabled while foregrounded; tap opts into manual play. Idle 8dp/3 degrees and a 24dp reaction are more visible; small mascots scale movement down. Pause/background/disposal stop the frame loop. Consumed request prevents replaying old taps on resume. Images still move as a whole; no articulated limb animation or new 3D mesh.
- Source/tag v7 `b6cbc6868a5070f56dbb236ddf2250f7a5315ac7`. Debug verification `38746696833` and release candidate `38746696857` passed. 19 JVM tests (4 new motion regressions) cover manual playback with system auto motion off, pause/background dominance, distinct looping idle frames, and bounded tap reaction settling. UI/Choreographer and physical-device tests have not been performed.
- Reused draft staging `38747375811`, downloaded candidate and signed with the original fixed key. Removed unsigned draft asset before publishing. Distribution `ecda7c0b2f176df77109629b2af3b5f5bcbc5a54`; public release https://github.com/masakasakasama/German-language/releases/tag/v7. Publishers `38747636804` (APK/feed) and `38747636822` (ZIP) passed.
- APK 4,514,345 bytes; SHA256 `3c7ae40aa7cc94698650cb1400f51e5e0fd31c6480d680324ac73a7cc8dca429`. Public raw ZIP 4,055,760 bytes downloaded, extracted and matched. Public APK/feed/latest-v7 checked; package/version7/0.1.7 and accepted artwork visible pixels/alpha verified. Certificate SHA256 unchanged: `f2ff2596fd6555775a537810658021b56fedfc3cbbbf64c84f2c73f89e76dbf2`.
