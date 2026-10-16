"""Build a genuine volume mesh, weighted skeleton and baked Android clips. Blender 4.3."""
import bpy, math, json, struct, sys
from pathlib import Path
from mathutils import Vector, Matrix
R=Path(__file__).resolve().parents[1]; OUT=R/'art/mascot/rigged'; ASSET=R/'app/src/main/assets/companion3d'
OUT.mkdir(exist_ok=True,parents=True);ASSET.mkdir(exist_ok=True,parents=True)
bpy.ops.object.select_all(action='SELECT');bpy.ops.object.delete(use_global=False)
# Coordinate tracing uses the accepted artwork; depth and invisible surfaces are authored.
def P(u,v,depth=0):return Vector(((u-.5)*3.0,depth,(1-v)*2.5))
parts=[]
source=bpy.data.images.load(str(R/'app/src/main/res/drawable-nodpi/travel_companion.png'))
colors={'fur':(.91,.85,.74,1),'coat':(.018,.075,.16,1),'red':(.64,.025,.035,1),'leather':(.25,.10,.025,1),'gold':(.68,.40,.12,1),'sole':(.65,.34,.13,1),'black':(.025,.025,.025,1),'pink':(.91,.48,.45,1)}
materials={}
for name,col in colors.items():
 m=bpy.data.materials.new(name);m.use_nodes=True;n=m.node_tree.nodes;n.clear();l=m.node_tree.links
 out=n.new('ShaderNodeOutputMaterial');emit=n.new('ShaderNodeEmission');mix=n.new('ShaderNodeMixRGB');mix.blend_type='MIX';mix.inputs[1].default_value=col
 uv=n.new('ShaderNodeUVMap');uv.uv_map='ReferenceUV';im=n.new('ShaderNodeTexImage');im.image=source;im.extension='EXTEND';l.new(uv.outputs['UV'],im.inputs['Vector'])
 att=n.new('ShaderNodeVertexColor');att.layer_name='ProjectionWeight';mul=n.new('ShaderNodeMath');mul.operation='MULTIPLY';l.new(att.outputs['Color'],mul.inputs[0]);l.new(im.outputs['Alpha'],mul.inputs[1]);l.new(mul.outputs[0],mix.inputs[0]);l.new(im.outputs['Color'],mix.inputs[2]);l.new(mix.outputs[0],emit.inputs[0]);l.new(emit.outputs[0],out.inputs[0]);materials[name]=m
# Skeleton pivots in the same reference space. All bones are real deform bones.
spec=[('root',None,P(.5,.98),P(.5,.85)),('pelvis','root',P(.49,.74),P(.49,.66)),('chest','pelvis',P(.49,.66),P(.48,.53)),('head','chest',P(.48,.51),P(.48,.32)),
 ('ear.L','head',P(.48,.19,.03),P(.60,.04,.03)),('ear.R','head',P(.60,.28,.04),P(.80,.20,.04)),
 ('upper_arm.L','chest',P(.345,.50),P(.285,.44)),('forearm.L','upper_arm.L',P(.285,.44),P(.235,.40)),('hand.L','forearm.L',P(.235,.40),P(.205,.36)),
 ('upper_arm.R','chest',P(.55,.53),P(.63,.59)),('forearm.R','upper_arm.R',P(.63,.59),P(.685,.63)),('hand.R','forearm.R',P(.685,.63),P(.72,.63)),
 ('thigh.L','pelvis',P(.43,.75),P(.39,.83)),('shin.L','thigh.L',P(.39,.83),P(.40,.92)),('foot.L','shin.L',P(.40,.92),P(.40,.95,-.10)),
 ('thigh.R','pelvis',P(.53,.75),P(.57,.83)),('shin.R','thigh.R',P(.57,.83),P(.56,.92)),('foot.R','shin.R',P(.56,.92),P(.56,.95,-.10)),
 ('case','hand.R',P(.72,.63),P(.80,.83)),('backpack','chest',P(.51,.59,.23),P(.51,.73,.23))]
armdata=bpy.data.armatures.new('TravelCompanionSkeleton');rig=bpy.data.objects.new('TravelCompanionRig',armdata);bpy.context.collection.objects.link(rig);bpy.context.view_layer.objects.active=rig;rig.select_set(True);bpy.ops.object.mode_set(mode='EDIT')
for name,parent,head,tail in spec:
 b=armdata.edit_bones.new(name);b.head=head;b.tail=tail
 if parent:b.parent=armdata.edit_bones[parent]
 # Consistent axes allow all exported animations to use authored world-axis poses.
 b.use_deform=True
bpy.ops.object.mode_set(mode='OBJECT');rig.select_set(False)
names=[s[0] for s in spec];ids={n:i for i,n in enumerate(names)}
def add_mesh(name,verts,faces,mat,weights,projection=True):
 mesh=bpy.data.meshes.new(name);mesh.from_pydata(verts,[],faces);mesh.update();o=bpy.data.objects.new(name,mesh);bpy.context.collection.objects.link(o);mesh.materials.append(materials[mat]);parts.append(o)
 uv=mesh.uv_layers.new(name='ReferenceUV');color=mesh.color_attributes.new(name='ProjectionWeight',type='FLOAT_COLOR',domain='CORNER')
 for poly in mesh.polygons:poly.use_smooth=True
 for i,loop in enumerate(mesh.loops):
  v=mesh.vertices[loop.vertex_index];uv.data[i].uv=(v.co.x/3+.5,v.co.z/2.5)
  # Fade projection around side surfaces; full geometry remains opaque and closed.
  w=max(0,min(1,(-v.normal.y-.08)/.56)) if projection else 0
  color.data[i].color=(w,w,w,1)
 for i,ws in enumerate(weights):
  for bone,w in ws.items():
   group=o.vertex_groups.get(bone) or o.vertex_groups.new(name=bone);group.add([i],w,'REPLACE')
 mod=o.modifiers.new('Weighted skeleton','ARMATURE');mod.object=rig;o.parent=rig
 return o

def ellipsoid(name,uv,scale,mat,bone,tilt=0,depth=0,segments=40,rings=24,projection=True):
 center=P(*uv,depth);sx,sy,sz=scale;verts=[];faces=[];a=math.radians(tilt)
 for j in range(rings+1):
  lat=math.pi*j/rings
  for i in range(segments):
   phi=2*math.pi*i/segments;x=sx*math.sin(lat)*math.cos(phi);y=sy*math.sin(lat)*math.sin(phi);z=sz*math.cos(lat)
   verts.append(tuple(center+Vector((x*math.cos(a)+z*math.sin(a),y,-x*math.sin(a)+z*math.cos(a)))))
 for j in range(rings):
  for i in range(segments):
   a=j*segments+i;b=j*segments+(i+1)%segments;faces.append(tuple(reversed((a,b+segments,a+segments) if j==0 else (a,b,b+segments) if j==rings-1 else (a,b,b+segments,a+segments))))
 return add_mesh(name,verts,faces,mat,[{bone:1}]*len(verts),projection)

def tube(name,points,radii,mat,bones,depths=None,sides=24,steps=24,projection=True):
 ps=[P(*q,0 if depths is None else depths[i]) for i,q in enumerate(points)];verts=[];faces=[];weights=[]
 for j in range(steps+1):
  t=j/steps*(len(ps)-1);k=min(int(t),len(ps)-2);f=t-k;center=ps[k].lerp(ps[k+1],f);direction=(ps[k+1]-ps[k]).normalized();side=direction.cross(Vector((0,1,0))).normalized();forward=side.cross(direction).normalized();radius=radii[k]*(1-f)+radii[k+1]*f
  # Adjacent bones blend over the middle 40% around an anatomical joint.
  ws={bones[min(k,len(bones)-1)]:1.0}
  for boundary in range(1,len(bones)):
   if abs(t-boundary)<.28:
    blend=(t-boundary+.28)/.56;ws={bones[boundary-1]:1-blend,bones[boundary]:blend};break
  for i in range(sides):
   a=2*math.pi*i/sides;verts.append(tuple(center+radius*(math.cos(a)*side+math.sin(a)*forward)));weights.append(ws)
 for j in range(steps):
  for i in range(sides):
   a=j*sides+i;b=j*sides+(i+1)%sides;faces.append((a,a+sides,b+sides,b))
 faces.append(tuple(range(sides)));faces.append(tuple(reversed([steps*sides+i for i in range(sides)])))
 return add_mesh(name,verts,faces,mat,weights,projection)

def rounded_box(name,uv,scale,mat,bone,depth=0,tilt=0):
 bpy.ops.mesh.primitive_cube_add(size=2,location=P(*uv,depth));o=bpy.context.object;o.scale=scale;o.rotation_euler[1]=math.radians(tilt);bpy.ops.object.transform_apply(location=False,rotation=False,scale=True)
 mod=o.modifiers.new('Rounded sewn corners','BEVEL');mod.width=min(scale)*.32;mod.segments=5;bpy.ops.object.modifier_apply(modifier=mod.name)
 verts=[tuple(o.matrix_world@v.co) for v in o.data.vertices];faces=[tuple(p.vertices) for p in o.data.polygons];bpy.data.objects.remove(o,do_unlink=True)
 return add_mesh(name,verts,faces,mat,[{bone:1}]*len(verts))
# Tail and leather backpack are real rear geometry, visible when turning around.
ellipsoid('Tail',(.49,.78),(.13,.15,.13),'fur','pelvis',depth=.25)
rounded_box('Backpack',(.52,.63),(.29,.17,.37),'leather','backpack',depth=.23)
rounded_box('Backpack pocket',(.52,.70),(.23,.07,.14),'leather','backpack',depth=.42)
for u in [.43,.59]:tube('Leather shoulder strap',[(u,.52),(u,.60),(u-.01,.71)],[.026,.022,.026],'leather',['chest','chest'],depths=[-.15,-.25,-.16],projection=False)
ellipsoid('Body under coat',(.475,.68),(.40,.26,.38),'fur','pelvis')
ellipsoid('Tailored coat torso',(.465,.605),(.45,.28,.39),'coat','chest')
# Shirt and lapels have volume, rather than floating front-facing planes.
ellipsoid('Waistcoat',(.43,.625),(.17,.045,.29),'fur','chest',depth=-.27)
for u,tilt in [(.365,-20),(.505,23)]:ellipsoid('Coat lapel', (u,.535),(.07,.045,.19),'coat','chest',tilt,depth=-.29)
for u in [.377,.50]:
 for v in [.60,.66,.71]:ellipsoid('Brass button',(u,v),(.021,.012,.023),'gold','chest',depth=-.30,segments=16,rings=10,projection=False)
# Skin-weighted sleeves are continuous across the elbow, with overlapping cuff/hand roots.
tube('Left sleeve',[(.345,.50),(.285,.44),(.235,.40)],[.105,.102,.105],'coat',['upper_arm.L','forearm.L'])
tube('Right sleeve',[(.55,.53),(.63,.59),(.685,.63)],[.13,.115,.093],'coat',['upper_arm.R','forearm.R'])
for side,uv,tilt in [('L',(.235,.40),-40),('R',(.685,.63),-65)]:
 ellipsoid('Cuff '+side,uv,(.11,.11,.044),'gold','forearm.'+side,tilt)
ellipsoid('Left palm',(.207,.366),(.122,.082,.135),'fur','hand.L',-22,depth=-.015)
for u,v,r in [(.174,.365,.044),(.185,.330,.047),(.210,.325,.045),(.230,.340,.040)]:ellipsoid('Left finger',(u,v),(r,.067,r*1.3),'fur','hand.L',-22,depth=-.01,segments=20,rings=12)
ellipsoid('Right palm',(.715,.625),(.135,.09,.095),'fur','hand.R',-65,depth=-.01)
# Legs remain connected under the pelvis and are smoothly weighted at knees.
tube('Left leg',[(.43,.75),(.39,.83),(.40,.92)],[.155,.13,.10],'fur',['thigh.L','shin.L'])
tube('Right leg',[(.53,.75),(.57,.83),(.56,.92)],[.155,.13,.10],'fur',['thigh.R','shin.R'])
ellipsoid('Left foot',(.40,.93),(.15,.21,.09),'fur','foot.L',depth=-.06)
ellipsoid('Right foot',(.56,.93),(.15,.21,.09),'fur','foot.R',depth=-.06)
# Broad head silhouette and painted expression derive from the accepted art.
ellipsoid('Head',(.475,.36),(.565,.40,.345),'fur','head',13,depth=-.015,segments=64,rings=40)
# Ears are closed volume meshes with roots tucked into the beret.
ellipsoid('Left ear',(.56,.14),(.17,.095,.35),'fur','ear.L',48,depth=.035)
ellipsoid('Right ear',(.70,.235),(.19,.10,.36),'fur','ear.R',70,depth=.045)
ellipsoid('Beret',(.505,.245),(.52,.36,.145),'red','head',25,depth=.01)
ellipsoid('Beret brim',(.505,.285),(.50,.36,.035),'red','head',25,depth=-.005)
ellipsoid('Beret stem',(.43,.14),(.035,.04,.065),'red','head',25,depth=.01)
# Subtle muzzle and nose geometry: expression texture remains on curved facial surfaces.
ellipsoid('Muzzle',(.429,.353),(.13,.035,.08),'fur','head',depth=-.407)
ellipsoid('Nose',(.419,.331),(.033,.026,.021),'pink','head',depth=-.44,segments=24,rings=16)
# Luggage is bound to the gripping hand, so its handle follows instead of detaching.
rounded_box('Suitcase',(.80,.80),(.31,.13,.33),'red','case',depth=.02,tilt=-18)
for uv in [(.73,.94),(.92,.89)]:ellipsoid('Luggage wheel',uv,(.05,.08,.06),'black','case',depth=.02,segments=20,rings=12,projection=False)
for a,b in [((.74,.68),(.72,.625)),((.79,.67),(.77,.61))]:tube('Handle rail',[a,b],[.016,.016],'black',['case'],projection=False,steps=4)
tube('Grip',[(.72,.625),(.77,.61)],[.023,.023],'red',['case'],projection=False,steps=4)
# Bone weights and topology stay editable. Shared atlas is baked from projection+authored rear colors.
bpy.ops.object.select_all(action='DESELECT')
for o in parts:o.select_set(True)
bpy.context.view_layer.objects.active=parts[0]
for o in parts:o.data.uv_layers.new(name='UVMap');o.data.uv_layers.active=o.data.uv_layers['UVMap'];o.data.uv_layers['UVMap'].active_render=True
bpy.ops.object.mode_set(mode='EDIT');bpy.ops.mesh.select_all(action='SELECT');bpy.ops.uv.smart_project(angle_limit=math.radians(65),island_margin=.015);bpy.ops.object.mode_set(mode='OBJECT')
# Pack islands across all component meshes into one atlas.
bpy.ops.object.mode_set(mode='EDIT');bpy.ops.uv.select_all(action='SELECT');bpy.ops.uv.pack_islands(rotate=True,margin=.004);bpy.ops.object.mode_set(mode='OBJECT')
# Reacquire UV references after adding custom-data layers: Blender reallocates layer storage.
pixels=list(source.pixels);iw,ih=source.size
for o in parts:
 mesh=o.data;uv=mesh.uv_layers['ReferenceUV'];color=mesh.color_attributes['ProjectionWeight']
 for i,loop in enumerate(mesh.loops):
  v=mesh.vertices[loop.vertex_index];uv.data[i].uv=(v.co.x/3+.5,v.co.z/2.5)
  w=max(0,min(1,(-v.normal.y-.08)/.56))
  if o.name.startswith(('Brass','Luggage wheel','Leather shoulder','Handle','Grip','Beret')):w=0
  if o.name=='Head':
   u,vv=uv.data[i].uv;xx=max(0,min(iw-1,int(u*iw)));yy=max(0,min(ih-1,int(vv*ih)));pixel=(yy*iw+xx)*4;rr,gg,bb=pixels[pixel:pixel+3]
   if (u>.55 and vv>.57 and rr>gg*1.4 and rr>bb*1.4) or vv>.75:w=0
  color.data[i].color=(w,w,w,1)
atlas=bpy.data.images.new('CompanionAlbedo',width=2048,height=2048,alpha=False)
for m in materials.values():node=m.node_tree.nodes.new('ShaderNodeTexImage');node.image=atlas;m.node_tree.nodes.active=node
scene=bpy.context.scene;scene.render.engine='CYCLES';scene.cycles.samples=1;scene.render.bake.use_clear=False;scene.render.bake.margin=8
bpy.ops.object.bake(type='EMIT')
atlas.filepath_raw=str(ASSET/'albedo.png');atlas.file_format='PNG';atlas.save()
# Export-friendly PBR material: same baked texture for GLB, Blender and Android.
mat=bpy.data.materials.new('Companion baked albedo');mat.use_nodes=True;nodes=mat.node_tree.nodes;bs=nodes.get('Principled BSDF');bs.inputs['Roughness'].default_value=.80;im=nodes.new('ShaderNodeTexImage');im.image=atlas;mat.node_tree.links.new(im.outputs['Color'],bs.inputs['Base Color'])
for o in parts:o.data.materials.clear();o.data.materials.append(mat)
# Pose channels use world rest axes converted into bone-local rotation axes.
def rotation(name,axis,deg):
 b=rig.pose.bones[name];local=armdata.bones[name].matrix_local.to_3x3().inverted()@Vector(axis);b.rotation_mode='QUATERNION';b.rotation_quaternion=__import__('mathutils').Quaternion(local,math.radians(deg))
def pose(clip,t):
 for b in rig.pose.bones:b.rotation_mode='QUATERNION';b.rotation_quaternion=(1,0,0,0);b.location=(0,0,0);b.scale=(1,1,1)
 # Move from the reference greeting pose into a relaxed, balanced standing posture.
 rotation('upper_arm.L',(0,1,0),-95)
 rotation('foot.L',(0,1,0),0)
 phase=t*2*math.pi
 idle_phase=phase if clip=='Walk' else phase/3.6;breath=math.sin(idle_phase);rig.pose.bones['chest'].scale=(1+.008*breath,1+.006*breath,1+.008*breath)
 rotation('head',(0,0,1),2*math.sin(idle_phase));rotation('ear.L',(0,1,0),3*math.sin(idle_phase-.4));rotation('ear.R',(0,1,0),-2*math.sin(idle_phase-.6))
 if clip=='Wave':
  e=math.sin(math.pi*t/2.4)**2;rotation('upper_arm.L',(0,1,0),-95+95*e);rotation('forearm.L',(0,1,0),18*math.sin(phase*1.7)*e);rotation('hand.L',(0,1,0),20*math.sin(phase*1.7+.3)*e)
 if clip=='Walk':
  # Gait is in depth: feet really swing forward/back, visible from side or back.
  stride=math.sin(phase);rotation('thigh.L',(1,0,0),26*stride);rotation('thigh.R',(1,0,0),-26*stride)
  # Compose the bind-to-standing frontal leg correction with sagittal swing.
  q=__import__('mathutils').Quaternion(armdata.bones['thigh.L'].matrix_local.to_3x3().inverted()@Vector((0,1,0)),math.radians(0));rig.pose.bones['thigh.L'].rotation_quaternion=q@rig.pose.bones['thigh.L'].rotation_quaternion
  rotation('shin.L',(1,0,0),-35*max(0,-stride));rotation('shin.R',(1,0,0),-35*max(0,stride));rotation('upper_arm.R',(1,0,0),12*stride)
  # Left arm opposite to the corresponding leg, with its relaxed frontal correction retained.
  q=rig.pose.bones['upper_arm.L'].rotation_quaternion.copy();rotation('upper_arm.L',(1,0,0),-15*stride);rig.pose.bones['upper_arm.L'].rotation_quaternion=q@rig.pose.bones['upper_arm.L'].rotation_quaternion
  rig.pose.bones['root'].location.y=.025*(1-math.cos(phase*2));rotation('forearm.R',(0,1,0),-12)
 if clip=='Jump':
  crouch=math.sin(math.pi*t/.4)**2 if t<.4 else .8*math.sin(math.pi*(t-1)/.4)**2 if 1<t<1.4 else 0
  flight=max(0,min(1,(t-.4)/.6));height=.35*4*flight*(1-flight) if .4<=t<=1 else 0
  rig.pose.bones['root'].location.y=height-.075*crouch
  tuck=math.sin(math.pi*flight) if .4<=t<=1 else 0
  for side in ['L','R']:
   q=rig.pose.bones['thigh.'+side].rotation_quaternion.copy();rotation('thigh.'+side,(1,0,0),24*crouch+15*tuck);rig.pose.bones['thigh.'+side].rotation_quaternion=q@rig.pose.bones['thigh.'+side].rotation_quaternion;rotation('shin.'+side,(1,0,0),-42*crouch-28*tuck);rotation('foot.'+side,(1,0,0),18*crouch+13*tuck)
  rotation('ear.L',(0,1,0),-12*height+7*crouch);rotation('ear.R',(0,1,0),10*height-6*crouch)
 bpy.context.view_layer.update()
 # Plant the support foot during gait and crouch; airborne phase retains its trajectory.
 if clip=='Walk' or (clip=='Jump' and (t<.4 or t>1)):
  minimum=100.0
  for o in parts:
   if o.name not in ['Left foot','Right foot']:continue
   bone=rig.pose.bones['foot.L' if o.name=='Left foot' else 'foot.R'];m=bone.matrix@bone.bone.matrix_local.inverted()
   minimum=min(minimum,min((m@v.co).z for v in o.data.vertices))
  rig.pose.bones['root'].location.y+=.085-minimum
  bpy.context.view_layer.update()
# Export triangles with four skin influences and a shared atlas UV.
blob=bytearray();ranges=[]
for o in parts:
 mesh=o.data;mesh.calc_loop_triangles();start=len(blob)//64
 for tri in mesh.loop_triangles:
  for vi,li in zip(tri.vertices,tri.loops):
   v=mesh.vertices[vi];p=v.co;n=v.normal;uv=mesh.uv_layers['UVMap'].data[li].uv
   weights=sorted([(ids[o.vertex_groups[g.group].name],g.weight) for g in v.groups],key=lambda q:-q[1])[:4];weights+= [(0,0)]*(4-len(weights));total=sum(w for _,w in weights)
   blob.extend(struct.pack('<16f',p.x,p.z,-p.y,n.x,n.z,-n.y,uv.x,uv.y,*[float(i) for i,_ in weights],*[w/total for _,w in weights]))
 ranges.append({'name':o.name,'first':start,'count':len(blob)//64-start})
(ASSET/'model.bin').write_bytes(blob)
C=Matrix(((1,0,0,0),(0,0,1,0),(0,-1,0,0),(0,0,0,1)))
anim=bytearray();clips=[]
for clip,duration in [('Idle',3.6),('Wave',2.4),('Walk',1.0),('Jump',1.4)]:
 action=bpy.data.actions.new(clip);rig.animation_data_create();rig.animation_data.action=action
 frames=round(duration*30)+1;offset=len(anim)//4
 for f in range(frames):
  t=f/30;pose(clip,t)
  for name in names:
   b=rig.pose.bones[name];b.keyframe_insert('rotation_quaternion',frame=f+1,group=name);b.keyframe_insert('location',frame=f+1,group=name);b.keyframe_insert('scale',frame=f+1,group=name)
   m=C@(b.matrix@armdata.bones[name].matrix_local.inverted())@C.inverted()
   anim.extend(struct.pack('<16f',*[m[row][col] for col in range(4) for row in range(4)]))
 clips.append({'name':clip,'frames':frames,'fps':30,'offset':offset,'loop':clip in ['Idle','Walk']})
 track=rig.animation_data.nla_tracks.new();track.name=clip;track.strips.new(clip,1,action);rig.animation_data.action=None
 for tr in rig.animation_data.nla_tracks:tr.mute=True
(ASSET/'animation.bin').write_bytes(anim)
(ASSET/'model.json').write_text(json.dumps({'format':1,'stride':64,'bones':names,'parents':[ids[parent] if parent else -1 for _,parent,_,_ in spec],'bindHeads':[[p.x,p.z,-p.y] for _,_,p,_ in spec],'parts':ranges,'clips':clips},indent=2))
# Export bones, skin and exact authored animation actions as portable GLB.
for tr in rig.animation_data.nla_tracks:tr.mute=False
bpy.ops.object.select_all(action='DESELECT');rig.select_set(True)
for o in parts:o.select_set(True)
bpy.context.view_layer.objects.active=rig
scene.render.fps=30;scene.frame_set(1)
bpy.ops.export_scene.gltf(filepath=str(OUT/'travel-companion.glb'),use_selection=True,export_format='GLB',export_animation_mode='NLA_TRACKS',export_skins=True,export_all_influences=False)
for tr in rig.animation_data.nla_tracks:tr.mute=True
pose('Idle',0)
# Preview cameras show actual volume from front, side and rear.
bpy.ops.object.camera_add(location=(0,-7,1.5));cam=bpy.context.object;cam.data.type='ORTHO';cam.data.ortho_scale=3.4;scene.camera=cam
scene.render.engine='CYCLES';scene.cycles.samples=24;scene.cycles.use_denoising=False;scene.render.resolution_x=720;scene.render.resolution_y=720;scene.render.resolution_percentage=100;scene.world.color=(.5,.5,.5);scene.view_settings.view_transform='Standard'
for pos in [(-3,-4,6),(4,-2,4),(0,4,5)]:
 bpy.ops.object.light_add(type='AREA',location=pos);o=bpy.context.object;o.data.energy=220;o.data.size=5;o.rotation_euler=(Vector((0,0,1.2))-o.location).to_track_quat('-Z','Y').to_euler()
for label,ang in [('front',0),('side',90),('back',180),('three-quarter',35)]:
 a=math.radians(ang);cam.location=(7*math.sin(a),-7*math.cos(a),1.45);cam.rotation_euler=(Vector((0,0,1.25))-cam.location).to_track_quat('-Z','Y').to_euler();scene.render.filepath=str(OUT/(label+'.png'));bpy.ops.render.render(write_still=True)
source.pack();atlas.pack();bpy.ops.wm.save_as_mainfile(filepath=str(OUT/'travel-companion.blend'))
print('RIGGED_MODEL',len(parts),'parts',len(names),'bones',len(blob)//192,'triangles')
