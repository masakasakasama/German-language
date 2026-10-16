"""Structural checks for real volume geometry, skin attachment and reusable clips."""
import json,struct
from pathlib import Path
import numpy as np
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets/companion3d'
meta=json.loads((A/'model.json').read_text());v=np.frombuffer((A/'model.bin').read_bytes(),dtype='<f4').reshape(-1,16);anim=np.frombuffer((A/'animation.bin').read_bytes(),dtype='<f4');n=len(meta['bones'])
assert np.isfinite(v).all() and np.isfinite(anim).all()
assert n==20 and len(meta['parts'])>=40
assert np.allclose(np.linalg.norm(v[:,3:6],axis=1),1,atol=.002)
assert np.allclose(v[:,12:].sum(axis=1),1,atol=.0001)
assert (v[:,12:]>=0).all() and (v[:,8:12]>=0).all() and (v[:,8:12]<n).all()
for p in meta['parts']:
 part=v[p['first']:p['first']+p['count']]
 assert len(part)==p['count'] and len(part)%3==0
 assert np.ptp(part[:,:3],axis=0).min()>.01,(p['name'],'flat component')
for c in meta['clips']:
 m=anim[c['offset']:c['offset']+c['frames']*n*16].reshape(c['frames'],n,4,4).transpose(0,1,3,2)
 if c['loop']:assert np.allclose(m[0],m[-1],atol=.0001),(c['name'],'loop seam')
 assert np.max(np.abs(m[0]-m[len(m)//3]))>.001,(c['name'],'no movement')
 for i,parent in enumerate(meta['parents']):
  if parent<0:continue
  point=np.array([*meta['bindHeads'][i],1]);a=m[:,i]@point;b=m[:,parent]@point
  assert np.max(np.abs(a-b))<.0001,(c['name'],meta['bones'][i],'detached joint')
 if c['name']=='Walk':
  heights=[]
  for frame in m:
   low=100
   for p in meta['parts']:
    if p['name'] not in ['Left foot','Right foot']:continue
    part=v[p['first']:p['first']+p['count']];pos=np.c_[part[:,:3],np.ones(len(part))];bone=meta['bones'].index('foot.L' if p['name']=='Left foot' else 'foot.R')
    low=min(low,(pos@frame[bone].T)[:,1].min())
   heights.append(low)
  assert np.max(np.abs(np.array(heights)-.085))<.0002,heights
raw=(R/'art/mascot/rigged/travel-companion.glb').read_bytes();assert raw[:4]==b'glTF'
size,kind=struct.unpack_from('<II',raw,12);g=json.loads(raw[20:20+size]);assert g.get('skins')
clipnames={a['name'] for a in g['animations']};assert {'Idle','Wave','Walk','Jump'}<=clipnames,clipnames
print('PASS:',len(meta['parts']),'volume parts;',n,'bones; normalized weights; attached joints; grounded gait; GLB skins and',sorted(clipnames))
