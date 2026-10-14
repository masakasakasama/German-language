import os
os.environ['PYOPENGL_PLATFORM']='egl';os.environ['EGL_PLATFORM']='surfaceless'
from OpenGL.EGL import *
from OpenGL.GL import *
import ctypes,json,numpy as np,re
from pathlib import Path
from PIL import Image
root=Path(__file__).resolve().parents[1]
d=eglGetDisplay(EGL_DEFAULT_DISPLAY);major=EGLint();minor=EGLint();assert eglInitialize(d,major,minor)
attrs=(EGLint*13)(EGL_SURFACE_TYPE,EGL_PBUFFER_BIT,EGL_RENDERABLE_TYPE,EGL_OPENGL_ES2_BIT,EGL_RED_SIZE,8,EGL_GREEN_SIZE,8,EGL_BLUE_SIZE,8,EGL_DEPTH_SIZE,24,EGL_NONE)
c=EGLConfig();num=EGLint();assert eglChooseConfig(d,attrs,ctypes.byref(c),1,num)
ctx=eglCreateContext(d,c,EGL_NO_CONTEXT,(EGLint*3)(EGL_CONTEXT_CLIENT_VERSION,2,EGL_NONE))
w,h=800,520
surf=eglCreatePbufferSurface(d,c,(EGLint*5)(EGL_WIDTH,w,EGL_HEIGHT,h,EGL_NONE));assert eglMakeCurrent(d,surf,surf,ctx)
src=(root/'app/src/main/java/com/masakasakasama/reisedeutsch/ui/Bunny3D.kt').read_text()
shaders=re.findall(r'shader\(GL_\w+_SHADER, """(.*?)"""',src,re.S)
p=glCreateProgram()
for code,kind in zip(shaders,[GL_VERTEX_SHADER,GL_FRAGMENT_SHADER]):
 s=glCreateShader(kind);glShaderSource(s,code);glCompileShader(s);assert glGetShaderiv(s,GL_COMPILE_STATUS),glGetShaderInfoLog(s);glAttachShader(p,s)
glBindAttribLocation(p,0,b'aPosition');glBindAttribLocation(p,1,b'aNormal');glLinkProgram(p);assert glGetProgramiv(p,GL_LINK_STATUS);glUseProgram(p)
data=np.frombuffer((root/'app/src/main/assets/mascot/bunny.bin').read_bytes(),dtype=np.float32)
b=GLuint();glGenBuffers(1,ctypes.byref(b));glBindBuffer(GL_ARRAY_BUFFER,b);glBufferData(GL_ARRAY_BUFFER,data.nbytes,data,GL_STATIC_DRAW)
glEnableVertexAttribArray(0);glEnableVertexAttribArray(1);glVertexAttribPointer(0,3,GL_FLOAT,False,24,ctypes.c_void_p(0));glVertexAttribPointer(1,3,GL_FLOAT,False,24,ctypes.c_void_p(12))
glEnable(GL_DEPTH_TEST);glViewport(0,0,w,h);glClearColor(1,.965,.9,1)
# Match app camera, projection, rotation and geometry.
eye=np.array([0,1.65,7]);target=np.array([0,1.5,0]);f=target-eye;f/=np.linalg.norm(f);side=np.cross(f,[0,1,0]);side/=np.linalg.norm(side);up=np.cross(side,f)
v=np.eye(4);v[:3,:3]=np.array([side,up,-f]);v[:3,3]=-v[:3,:3]@eye
hh=1.65;hw=hh*w/h;proj=np.diag([1/hw,1/hh,-2/14,1]);proj[2,3]=-16/14
angle=np.deg2rad(-12);m=np.eye(4);m[0,0]=m[2,2]=np.cos(angle);m[0,2]=np.sin(angle);m[2,0]=-np.sin(angle)
scene=json.loads((root/'app/src/main/assets/mascot/bunny.json').read_text())
for frame,a in [('idle',0),('wave',24)]:
 glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT)
 arm=np.eye(4);t=np.eye(4);t[:3,3]=scene['wavePivot'];r=np.eye(4);rad=np.deg2rad(a);r[0,0]=r[1,1]=np.cos(rad);r[0,1]=-np.sin(rad);r[1,0]=np.sin(rad);arm=m@t@r@np.linalg.inv(t)
 for part in scene['parts']:
  mat=arm if part['wave'] else m
  glUniformMatrix4fv(glGetUniformLocation(p,b'uMvp'),1,False,np.ascontiguousarray((proj@v@mat).T,dtype=np.float32));glUniformMatrix4fv(glGetUniformLocation(p,b'uModel'),1,False,np.ascontiguousarray(mat.T,dtype=np.float32));glUniform4fv(glGetUniformLocation(p,b'uColor'),1,np.array(part['color'],dtype=np.float32));glDrawArrays(GL_TRIANGLES,part['first'],part['count'])
 pixels=np.zeros((h,w,4),dtype=np.uint8);glReadPixels(0,0,w,h,GL_RGBA,GL_UNSIGNED_BYTE,pixels);assert glGetError()==GL_NO_ERROR
 Image.fromarray(pixels[::-1]).save(root/f'art/mascot/runtime-{frame}.png')
 if frame=='idle':first=pixels.copy()
 else:assert np.count_nonzero(first!=pixels)>1000
print('PASS GLES2 shader compile/link, depth rendering, idle and wave frames differ')
