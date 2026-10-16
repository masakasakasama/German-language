# ReiseDeutsch — rigged 3D companion

This model has **44 editable volume components, 20 deform bones and 52,368 triangles**. It is not a plane, a cutout billboard or a 2D image deformation. It uses the accepted artwork to project front surface color onto authored 3D volumes. Side/back geometry and colors are reconstructed, not inferred ground truth from unseen reference views. It is a stylized reconstruction, not a scan or pixel-identical reproduction.

- `travel-companion.blend`: separate named meshes, vertex weights, armature and animation actions; source image and baked albedo packed. Open in Blender 4.3+.
- `travel-companion.glb`: same volumes and skeleton, PBR albedo, Idle/Wave/Walk/Jump clips. Import into Blender or a glTF viewer.
- `runtime-*.png`, `motion.mp4`: actual Android GLES2 shader, mesh and baked bone matrices rendered offscreen on Mesa. These are not Android device screenshots/recordings.
- `front.png`, `side.png`, `back.png`, `three-quarter.png`: offline Blender studio renders.

The head, ears, clothing, hands, feet, leather backpack, handle and suitcase have depth. Sleeves and legs blend adjacent bone weights. Hands/feet overlap their cuff/ankle roots. Case bone follows the gripping hand. Walk has sagittal leg swing, knee flexion, counter-swing of the arms and correction of the support foot height; it is an in-place gait, not navigation around the screen. Jump includes preparation, airborne leg flexion and landing compression. The mouth expression is textured; no speech lip-sync or full facial rig is provided.

Android uses `app/src/main/assets/companion3d/{model.bin,model.json,animation.bin,albedo.png}`. Binary vertex format: little-endian float32 XYZ, normal XYZ, UV, four joint indices as floats, four normalized weights (64 bytes/vertex); triangles are expanded. Animation matrices are column-major, generated from the same Blender skeleton and sampled at 30 fps. App uploads the matrices to a GLES2 skinning shader; no second approximate animation implementation.

Regenerate from repository root:

```sh
blender -b --python tools/build_rigged_companion.py
python3 tools/validate_rigged_companion.py
python3 tools/render_rigged_companion.py
```

Validation needs numpy; offscreen rendering also needs PyOpenGL, Pillow and Mesa EGL. Blender build must support Cycles; denoising is disabled for portability. Preserve original artwork at `app/src/main/res/drawable-nodpi/travel_companion.png`. The generator intentionally retains a second UV map for reference projection; glTF Validator may report unused first UV coordinates as informational entries.

All character placements use `RiggedCompanion`; drag horizontally or use left/front/right controls to rotate. Stop/background pauses frame requests. Disabled system animations start paused, with explicit playback allowed. Failure to initialize 3D displays the accepted still image and an explanatory message on the main character card. No new network permission/service is required.

Verified: volume bounds, normalized weights, shared joint positions throughout clips, grounded support foot during gait, loop closure, GLB skin/animations, GLES2 compile/link and front/side/back/action renders. Physical-device rendering, scrolling composition and performance remain untested.
