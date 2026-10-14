"""Deterministic embedded fabric/leather textures, shared by Blender and GLB."""
import bpy
import numpy as np

def material(name, hex_color):
    color = np.array([int(hex_color[i:i+2],16)/255 for i in (0,2,4)])
    m=bpy.data.materials.new(name); m.diffuse_color=tuple(color)+(1,); m.use_nodes=True
    nodes=m.node_tree.nodes; links=m.node_tree.links; p=nodes.get('Principled BSDF')
    linear=np.where(color<=.04045,color/12.92,((color+.055)/1.055)**2.4)
    p.inputs['Base Color'].default_value=tuple(linear)+(1,)
    p.inputs['Roughness'].default_value=.8
    if name not in ['fur','red','blue','brown','gold']:return m
    n=256; y,x=np.mgrid[:n,:n]; rng=np.random.default_rng(sum(map(ord,name)))
    noise=rng.random((n,n)); fine=rng.random((n,n))
    if name=='fur':
        height=(noise+np.roll(noise,1,0)+np.roll(noise,2,0))/3; strength=.16; grain=.025; rough=.92
        p.inputs['Sheen Weight'].default_value=.45
    elif name in ['red','blue','gold']:
        height=.5*noise+.25*np.sin(x*np.pi/2)+.25*np.sin(y*np.pi/2);strength=.3;grain=.08;rough=.83
        p.inputs['Sheen Weight'].default_value=.28
    else:
        height=(noise+np.roll(noise,1,0)+np.roll(noise,1,1))/3;strength=.45;grain=.12;rough=.58
    pixels=np.ones((n,n,4),dtype=np.float32);pixels[:,:,:3]=np.clip(color[None,None,:]*(1-grain+grain*2*height[:,:,None]),0,1)
    tex=bpy.data.images.new(name+'_color',width=n,height=n);tex.pixels.foreach_set(pixels.ravel());tex.pack()
    node=nodes.new('ShaderNodeTexImage');node.image=tex;links.new(node.outputs['Color'],p.inputs['Base Color'])
    dx=(np.roll(height,-1,1)-np.roll(height,1,1))*strength;dy=(np.roll(height,-1,0)-np.roll(height,1,0))*strength
    normals=np.stack((-dx,-dy,np.ones_like(dx)),axis=-1);normals/=np.linalg.norm(normals,axis=-1,keepdims=True)
    pixels[:,:,:3]=normals*.5+.5
    normaltex=bpy.data.images.new(name+'_normal',width=n,height=n);normaltex.colorspace_settings.name='Non-Color';normaltex.pixels.foreach_set(pixels.ravel());normaltex.pack()
    normalnode=nodes.new('ShaderNodeTexImage');normalnode.image=normaltex
    normalmap=nodes.new('ShaderNodeNormalMap');links.new(normalnode.outputs['Color'],normalmap.inputs['Color']);links.new(normalmap.outputs['Normal'],p.inputs['Normal'])
    p.inputs['Roughness'].default_value=rough
    return m
