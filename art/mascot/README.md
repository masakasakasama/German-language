# ReiseDeutsch travel bunny — 3D source v2 — softer face

Original geometry authored from the project's existing bunny and supplied ReiseDeutsch reference: cream rabbit, red beret, navy travel coat, backpack and red suitcase. Stylized toy interpretation, not a scan or a pixel-identical reproduction of the watercolor reference.

- `reise-bunny.blend`: editable Blender 4.3 scene, materials, studio camera/lights and node animation tracks.
- `reise-bunny.glb`: portable glTF 2.0 model, materials and **Idle** / **Wave** animation clips. Import in Blender or a glTF-compatible viewer. Separate object-node rig; no skeletal skin, facial rig or VRM humanoid mapping.
- `runtime-idle.png`, `runtime-wave.png`: offscreen GLES2 render using the app's actual mesh and shader, not Android screenshots.
- `preview.png`: Blender studio render.
- 99 named mesh parts, 51,280 triangles. Front is -Y in Blender; glTF and Android are Y-up.

Rebuild from repository root: `blender -b --python tools/build_mascot.py`.
Validate: `python3 tools/validate_mascot.py`.
Optional render smoke test: install PyOpenGL, numpy, Pillow, with Mesa EGL available, then `python3 tools/render_mascot_smoke.py`.

Android uses pre-baked `app/src/main/assets/mascot/bunny.bin` (little-endian interleaved float32 XYZ/normal, 24 bytes per vertex) and `bunny.json` (draw ranges/colors/wave membership). Both are generated from the same Blender geometry as GLB. No downloaded resources or third-party 3D engine. Native animation recreates the Idle/Wave gestures; it is not a general GLB animation player. Arms rotate about the shoulder pivot, body bobs. Model can be animated further in Blender.

Home displays the character with a wave button and motion pause control. App lifecycle pauses the GL surface, and Compose disposal stops its frame loop. Android disabled animations are respected. GL initialization errors fall back to the existing 2D mascot. The renderer caps updates at approximately 30 fps. No database/schema/signing-key changes.

Validated mesh integrity, GLB animation channels, GLES2 shader compile/link, rendered idle/wave image difference, Android build/lint and existing unit tests. Device GPU behavior, scroll surface composition and real-device performance remain unverified.

Revision 2 removes the protruding muzzle/open circular mouth, paints cheek patches flush to the head, enlarges the head, shortens the torso/limbs and softens the native renderer lighting. Preview revision; the published APK v5 still has revision 1.
