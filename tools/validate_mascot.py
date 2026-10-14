"""Validate shipped mesh and interchange file without third-party dependencies."""
from pathlib import Path
import json, struct, math
root=Path(__file__).resolve().parents[1]
data=(root/'app/src/main/assets/mascot/bunny.bin').read_bytes()
scene=json.loads((root/'app/src/main/assets/mascot/bunny.json').read_text())
assert scene['format']==1 and scene['stride']==24 and len(data)%72==0
cursor=0
for p in scene['parts']:
 assert p['first']==cursor and p['count']%3==0 and p['count']>0
 assert len(p['color'])==4 and all(0<=v<=1 for v in p['color'])
 cursor+=p['count']
assert cursor*24==len(data)
assert sum(p['wave'] for p in scene['parts'])==4
vertices=list(struct.iter_unpack('<6f',data))
assert all(all(math.isfinite(v) for v in row) for row in vertices)
assert all(abs(sum(v*v for v in row[3:])-1)<.001 for row in vertices)
assert min(row[1] for row in vertices)>=0 and max(row[1] for row in vertices)<3.2
blob=(root/'art/mascot/reise-bunny.glb').read_bytes()
magic,version,length=struct.unpack_from('<III',blob)
assert magic==0x46546c67 and version==2 and length==len(blob)
n,kind=struct.unpack_from('<II',blob,12); assert kind==0x4e4f534a
gltf=json.loads(blob[20:20+n]); assert gltf['asset']['version']=='2.0'
assert {'Idle','Wave'} <= {a['name'] for a in gltf['animations']}
assert len(gltf['meshes'])==len(scene['parts'])
for a in gltf['animations']:
 assert a['channels'] and a['samplers']
 for s in a['samplers']:assert gltf['accessors'][s['input']]['count']>1
print(f"PASS: {len(scene['parts'])} mesh parts, {cursor//3} triangles, finite unit normals, contiguous buffers, GLB Idle/Wave clips")
