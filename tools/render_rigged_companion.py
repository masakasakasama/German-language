"""Render the actual Android GLES2 shaders, mesh and baked bone matrices offscreen."""
import os
os.environ['PYOPENGL_PLATFORM']='egl';os.environ['EGL_PLATFORM']='surfaceless'
from OpenGL.EGL import *
from OpenGL.GL import *
import ctypes,json,re,math
from pathlib import Path
import numpy as np
from PIL import Image
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets/companion3d';O=R/'art/mascot/rigged';F=Path('/tmp/reise-rigged-frames');F.mkdir(exist_ok=True)
d=eglGetDisplay(EGL_DEFAULT_DISPLAY);major=EGLint();minor=EGLint();assert eglInitialize(d,major,minor)
attrs=(EGLint*13)(EGL_SURFACE_TYPE,EGL_PBUFFER_BIT,EGL_RENDERABLE_TYPE,EGL_OPENGL_ES2_BIT,EGL_RED_SIZE,8,EGL_GREEN_SIZE,8,EGL_BLUE_SIZE,8,EGL_DEPTH_SIZE,24,EGL_NONE)
c=EGLConfig();num=EGLint();assert eglChooseConfig(d,attrs,ctypes.byref(c),1,num)
ctx=eglCreateContext(d,c,EGL_NO_CONTEXT,(EGLint*3)(EGL_CONTEXT_CLIENT_VERSION,2,EGL_NONE));w=h=640
surf=eglCreatePbufferSurface(d,c,(EGLint*5)(EGL_WIDTH,w,EGL_HEIGHT,h,EGL_NONE));assert eglMakeCurrent(d,surf,surf,ctx)
src=(R/'app/src/main/java/com/masakasakasama/reisedeutsch/ui/RiggedCompanion.kt').read_text();codes=re.findall(r'shader\(GL_\w+_SHADER,\s*"""(.*?)"""',src,re.S);assert len(codes)==2
program=glCreateProgram()
for code,kind in zip(codes,[GL_VERTEX_SHADER,GL_FRAGMENT_SHADER]):
 s=glCreateShader(kind);glShaderSource(s,code);glCompileShader(s);assert glGetShaderiv(s,GL_COMPILE_STATUS),glGetShaderInfoLog(s);glAttachShader(program,s)
for i,n in enumerate(['aPosition','aNormal','aUv','aJoints','aWeights']):glBindAttribLocation(program,i,n)
glLinkProgram(program);assert glGetProgramiv(program,GL_LINK_STATUS),glGetProgramInfoLog(program);glUseProgram(program)
vertices=np.frombuffer((A/'model.bin').read_bytes(),dtype='<f4');buffer=glGenBuffers(1);glBindBuffer(GL_ARRAY_BUFFER,buffer);glBufferData(GL_ARRAY_BUFFER,vertices.nbytes,vertices,GL_STATIC_DRAW)
for i,count,offset in [(0,3,0),(1,3,12),(2,2,24),(3,4,32),(4,4,48)]:glEnableVertexAttribArray(i);glVertexAttribPointer(i,count,GL_FLOAT,False,64,ctypes.c_void_p(offset))
tex=glGenTextures(1);glBindTexture(GL_TEXTURE_2D,tex);pixels=np.array(Image.open(A/'albedo.png').convert('RGBA'))[::-1].copy();glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,pixels.shape[1],pixels.shape[0],0,GL_RGBA,GL_UNSIGNED_BYTE,pixels)
for param in [GL_TEXTURE_MIN_FILTER,GL_TEXTURE_MAG_FILTER]:glTexParameteri(GL_TEXTURE_2D,param,GL_LINEAR)
for param in [GL_TEXTURE_WRAP_S,GL_TEXTURE_WRAP_T]:glTexParameteri(GL_TEXTURE_2D,param,GL_CLAMP_TO_EDGE)
glUniform1i(glGetUniformLocation(program,'uTexture'),0);glEnable(GL_DEPTH_TEST);glViewport(0,0,w,h);glClearColor(1,.965,.902,1)
meta=json.loads((A/'model.json').read_text());anim=np.frombuffer((A/'animation.bin').read_bytes(),dtype='<f4');clips={c['name']:c for c in meta['clips']};n=len(meta['bones'])
eye=np.array([0,1.4,7]);target=np.array([0,1.25,0]);f=target-eye;f/=np.linalg.norm(f);side=np.cross(f,[0,1,0]);side/=np.linalg.norm(side);up=np.cross(side,f);view=np.eye(4);view[:3,:3]=np.array([side,up,-f]);view[:3,3]=-view[:3,:3]@eye
proj=np.diag([1/1.6,1/1.6,-2/14,1]);proj[2,3]=-16/14
loc={k:glGetUniformLocation(program,k) for k in ['uBones','uMvp','uModel']}
def draw(clip,t,yaw,path):
 c=clips[clip];frame=min(c['frames']-1,round(t*c['fps']));off=c['offset']+frame*n*16;m=anim[off:off+n*16]
 angle=math.radians(yaw);model=np.eye(4);model[0,0]=model[2,2]=math.cos(angle);model[0,2]=math.sin(angle);model[2,0]=-math.sin(angle)
 glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);glUniformMatrix4fv(loc['uBones'],n,False,m);glUniformMatrix4fv(loc['uMvp'],1,False,np.ascontiguousarray((proj@view@model).T,dtype=np.float32));glUniformMatrix4fv(loc['uModel'],1,False,np.ascontiguousarray(model.T,dtype=np.float32));glDrawArrays(GL_TRIANGLES,0,len(vertices)//16)
 p=np.zeros((h,w,4),dtype=np.uint8);glReadPixels(0,0,w,h,GL_RGBA,GL_UNSIGNED_BYTE,p);assert glGetError()==GL_NO_ERROR;Image.fromarray(p[::-1]).save(path);return p
samples=[]
for label,yaw in [('front',0),('side',90),('back',180),('three-quarter',35)]:samples.append(draw('Idle',0,yaw,O/('runtime-'+label+'.png')))
assert np.count_nonzero(samples[0]!=samples[1])>10000
sequence=[('Idle',i/30,i*6) for i in range(60)]+[('Walk',(i%30)/30,55) for i in range(90)]+[('Jump',i/30,35) for i in range(43)]+[('Wave',i/30,15) for i in range(73)]
for i,(clip,t,yaw) in enumerate(sequence):draw(clip,t,yaw,F/f'{i:04}.png')
print('PASS GLES2 skinning/shader; front/side/back differ;',len(sequence),'actual model frames rendered')
