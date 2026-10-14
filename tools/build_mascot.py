"""Blender 4.3: blender -b --python tools/build_mascot.py. Original ReiseDeutsch mascot."""
import bpy, math, json, struct
from pathlib import Path
from mathutils import Vector
ROOT=Path(__file__).resolve().parents[1]; ART=ROOT/'art/mascot'; ASSET=ROOT/'app/src/main/assets/mascot'
bpy.ops.object.select_all(action='SELECT'); bpy.ops.object.delete(use_global=False)
colors={'fur':'fff6df','pink':'efac9e','red':'c63f45','blue':'234f72','gold':'e3b363','ink':'273344','brown':'9a633e','white':'ffffff','black':'20252c','yellow':'f4bf48'}
mats={}
for name,h in colors.items():
 m=bpy.data.materials.new(name); m.diffuse_color=tuple(int(h[i:i+2],16)/255 for i in (0,2,4))+(1,); m.use_nodes=True; p=m.node_tree.nodes.get('Principled BSDF'); p.inputs['Base Color'].default_value=m.diffuse_color; p.inputs['Roughness'].default_value=.72; mats[name]=m
root=bpy.data.objects.new('BunnyRoot',None); bpy.context.collection.objects.link(root)
arm=bpy.data.objects.new('WaveArm',None); bpy.context.collection.objects.link(arm); arm.parent=root; arm.location=(-.48,0,1.22)
parts=[]
def ball(name,loc,scale,mat,angle=0,parent=root):
 bpy.ops.mesh.primitive_uv_sphere_add(segments=24,ring_count=12,location=loc); o=bpy.context.object; o.name=name; o.scale=scale; o.rotation_euler[1]=angle; o.data.materials.append(mats[mat]); bpy.ops.object.transform_apply(location=False,rotation=False,scale=True)
 for p in o.data.polygons:p.use_smooth=True
 o.parent=parent
 if parent==arm:o.location=Vector(loc)-arm.location
 parts.append(o); return o
# Front is negative Y; rounded toy-like silhouette.
ball('Tail',(0,.36,.68),(.23,.23,.23),'fur')
ball('Backpack',(0,.37,1.03),(.42,.22,.43),'brown')
for x in [-.25,.25]:
 ball('Foot',(x,-.10,.22),(.23,.32,.18),'fur')
 ball('Leg',(x,0,.43),(.19,.20,.29),'fur')
ball('Coat',(0,0,.92),(.48,.30,.55),'blue')
ball('Shirt',(0,-.285,.99),(.19,.055,.37),'fur')
for x in [-.19,.19]:ball('Lapel',(x,-.30,1.22),(.09,.045,.23),'gold',-.32 if x<0 else .32)
for z in [.78,.97,1.16]:ball('Button',(.13,-.35,z),(.035,.024,.035),'gold')
ball('RightSleeve',(.51,0,1.06),(.18,.20,.31),'blue',-.5)
ball('RightPaw',(.64,-.03,.84),(.16,.16,.18),'fur')
ball('WaveSleeve',(-.64,0,1.40),(.17,.18,.30),'blue',-.7,arm)
ball('WaveCuff',(-.79,-.01,1.58),(.16,.18,.075),'gold',-.7,arm)
ball('WavePaw',(-.85,-.01,1.72),(.17,.14,.20),'fur',-.3,arm)
ball('PawPad',(-.85,-.145,1.72),(.075,.018,.09),'pink',0,arm)
ball('Head',(0,-.035,1.81),(.59,.40,.52),'fur')
for x,a in [(-.29,-.22),(.30,.30)]:
 ball('Ear',(x,.015,2.45),(.18,.12,.53),'fur',a)
 ball('InnerEar',(x,-.103,2.46),(.105,.025,.39),'pink',a)
ball('BeretBand',(0,-.025,2.14),(.58,.39,.095),'red')
ball('Beret',(-.075,.01,2.27),(.52,.36,.17),'red',-.14)
ball('BeretStem',(.12,0,2.44),(.055,.05,.10),'red',.3)
for x in [-.235,.235]:
 # Happy curved eyes made from small overlapping spheres, no texture dependency.
 for i in range(9):
  t=math.pi*i/8; ball('HappyEye',(x+.073*math.cos(t),-.410,1.85+.047*math.sin(t)),(.018,.019,.019),'ink')
 ball('Blush',(x*1.48,-.37,1.70),(.105,.035,.06),'pink')
ball('Muzzle',(0,-.41,1.68),(.22,.06,.13),'fur')
ball('Smile',(0,-.470,1.67),(.082,.018,.072),'ink')
ball('Tongue',(0,-.49,1.635),(.048,.009,.027),'pink')
ball('Nose',(0,-.477,1.76),(.046,.025,.031),'pink')
# Rounded red travel case, handle and wheels.
ball('Suitcase',(.88,0,.46),(.27,.19,.35),'red',-.08)
for x in [.73,1.02]:ball('CaseWheel',(x,.02,.135),(.07,.08,.07),'ink')
for x in [.79,.96]:ball('HandlePost',(x,0,.83),(.025,.025,.13),'brown')
ball('CaseHandle',(.875,0,.95),(.11,.04,.035),'brown')
for z,col in [(.58,'black'),(.52,'red'),(.46,'yellow')]:ball('Flag',(.88,-.187,z),(.14,.02,.035),col)
ball('TravelSticker',(.84,-.195,.32),(.075,.019,.065),'white')
bpy.context.view_layer.update()
# Runtime export: world-space triangles (X,Y-up,Z-front), normals and named groups.
blob=bytearray(); ranges=[]
for o in parts:
 mesh=o.data; mesh.calc_loop_triangles(); start=len(blob)//24; normal=o.matrix_world.to_3x3().inverted().transposed()
 for tri in mesh.loop_triangles:
  for vi in tri.vertices:
   v=o.matrix_world @ mesh.vertices[vi].co; n=(normal @ mesh.vertices[vi].normal).normalized()
   blob.extend(struct.pack('<6f',v.x,v.z,-v.y,n.x,n.z,-n.y))
 ranges.append({'name':o.name,'first':start,'count':len(blob)//24-start,'color':list(o.data.materials[0].diffuse_color),'wave':o.parent==arm})
ASSET.mkdir(parents=True,exist_ok=True); (ASSET/'bunny.bin').write_bytes(blob)
(ASSET/'bunny.json').write_text(json.dumps({'format':1,'stride':24,'wavePivot':[-.48,1.22,0],'parts':ranges},separators=(',',':')))
# Two reusable glTF animation clips, object-node rig (no skinning required).
for ob,name,prop,keys in [(root,'Idle','location',[(1,(0,0,0)),(31,(0,0,.035)),(61,(0,0,0))]),(arm,'Wave','rotation_euler',[(1,(0,0,0)),(9,(0,-.38,0)),(17,(0,.18,0)),(25,(0,-.38,0)),(33,(0,.18,0)),(41,(0,0,0))])]:
 for frame,value in keys:setattr(ob,prop,value); ob.keyframe_insert(data_path=prop,frame=frame)
 ob.animation_data.action.name=name
 track=ob.animation_data.nla_tracks.new(); track.name=name; track.strips.new(name,1,ob.animation_data.action); ob.animation_data.action=None
scene=bpy.context.scene; scene.render.fps=30; scene.frame_set(1)
bpy.ops.object.select_all(action='DESELECT')
for o in parts+[root,arm]:o.select_set(True)
bpy.context.view_layer.objects.active=root
ART.mkdir(parents=True,exist_ok=True)
bpy.ops.export_scene.gltf(filepath=str(ART/'reise-bunny.glb'),use_selection=True,export_format='GLB',export_animation_mode='NLA_TRACKS')
# Studio preview; excluded from GLB, retained in editable blend.
bpy.ops.mesh.primitive_plane_add(size=200,location=(0,0,.02)); floor=bpy.context.object; floor.name='StudioFloor'; floor.data.materials.append(mats['fur'])
bpy.ops.object.camera_add(location=(4,-9,4.1)); cam=bpy.context.object; cam.rotation_euler=(Vector((.1,0,1.45))-cam.location).to_track_quat('-Z','Y').to_euler(); cam.data.type='ORTHO'; cam.data.ortho_scale=3.65; scene.camera=cam
for loc,power,size in [((-3,-4,7),500,5),((4,-1,5),350,4),((0,4,6),500,3)]:
 bpy.ops.object.light_add(type='AREA',location=loc); l=bpy.context.object; l.data.energy=power; l.data.shape='DISK'; l.data.size=size; l.rotation_euler=(Vector((0,0,1))-l.location).to_track_quat('-Z','Y').to_euler()
scene.render.engine='CYCLES'; scene.cycles.samples=24; scene.cycles.use_denoising=False; scene.render.resolution_x=800; scene.render.resolution_y=800; scene.render.resolution_percentage=100; scene.world.color=(.32,.32,.32); scene.view_settings.view_transform='Standard'; scene.render.filepath=str(ART/'preview.png')
bpy.ops.wm.save_as_mainfile(filepath=str(ART/'reise-bunny.blend')); bpy.ops.render.render(write_still=True)
print('Exported',len(parts),'parts',len(blob)//72,'triangles')
