package com.masakasakasama.reisedeutsch.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.os.SystemClock
import android.view.MotionEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.masakasakasama.reisedeutsch.R
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs

/** Real closed volume meshes and weighted bones; no billboard or image warp. */
@Composable fun RiggedCompanion(modifier: Modifier, controls: Boolean = false) {
    val context=LocalContext.current
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    var active by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    var paused by remember { mutableStateOf(!ValueAnimator.areAnimatorsEnabled()) }
    var failed by remember { mutableStateOf(false) }
    val view=remember(context) { RiggedSurface(context,onTap={paused=false},onFailure={failed=true}) }
    DisposableEffect(view,lifecycle) {
        val observer=LifecycleEventObserver { _, event ->
            if(event==Lifecycle.Event.ON_RESUME) { active=true;view.onResume() }
            if(event==Lifecycle.Event.ON_PAUSE) { active=false;view.onPause() }
        }
        lifecycle.addObserver(observer)
        if(active)view.onResume()
        onDispose { lifecycle.removeObserver(observer);view.onPause() }
    }
    LaunchedEffect(active,paused,failed) {
        view.character.moving=active && !paused
        view.requestRender()
        while(active && !paused && !failed) { view.requestRender();delay(33) }
    }
    val play:(String)->Unit={ clip -> paused=false;view.character.play(clip);view.requestRender() }
    if(failed) {
        Image(painterResource(R.drawable.travel_companion),"旅のうさぎ",modifier)
        if(controls)Text("この端末で3Dを表示できませんでした。")
    } else {
        AndroidView(factory={view},modifier=modifier)
        if(controls) {
            Text("左右にドラッグすると、横や後ろから見られます。")
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) {
                TextButton(onClick={play("Wave")}){Text("手を振る")}
                TextButton(onClick={play("Walk")}){Text("歩く")}
                TextButton(onClick={play("Jump")}){Text("ジャンプ")}
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) {
                TextButton(onClick={view.character.yaw-=45f;view.requestRender()}){Text("左へ回す")}
                TextButton(onClick={view.character.yaw=0f;view.requestRender()}){Text("正面")}
                TextButton(onClick={view.character.yaw+=45f;view.requestRender()}){Text("右へ回す")}
            }
            TextButton(onClick={paused=!paused}){Text(if(paused)"動きを再開" else "動きを止める")}
        }
    }
}

private class RiggedSurface(context:Context,private val onTap:()->Unit,onFailure:()->Unit):GLSurfaceView(context) {
    val character=RiggedRenderer(context) { post { onFailure() } }
    private var downX=0f;private var downY=0f;private var lastX=0f;private var turning=false
    init {
        setEGLContextClientVersion(2);preserveEGLContextOnPause=true;setRenderer(character)
        renderMode=RENDERMODE_WHEN_DIRTY
        contentDescription="立体の旅うさぎ。左右へのドラッグで向きを変更"
        isClickable=true
    }
    override fun onTouchEvent(event:MotionEvent):Boolean {
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN->{downX=event.x;lastX=event.x;downY=event.y;turning=false;return true}
            MotionEvent.ACTION_MOVE->{
                val dx=event.x-downX;val dy=event.y-downY
                if(!turning && abs(dx)>12 && abs(dx)>abs(dy)*1.3f){turning=true;parent?.requestDisallowInterceptTouchEvent(true)}
                if(turning){character.yaw+=(event.x-lastX)*.45f;requestRender()}
                lastX=event.x;return true
            }
            MotionEvent.ACTION_UP->{parent?.requestDisallowInterceptTouchEvent(false);if(!turning)performClick();return true}
            MotionEvent.ACTION_CANCEL->{parent?.requestDisallowInterceptTouchEvent(false);return true}
        }
        return super.onTouchEvent(event)
    }
    override fun performClick():Boolean { super.performClick();onTap();character.play("Wave");requestRender();return true }
}

private data class RigClip(val name:String,val frames:Int,val fps:Int,val offset:Int,val loop:Boolean)
private class RigData(context:Context) {
    val metadata=JSONObject(context.assets.open("companion3d/model.json").bufferedReader().use{it.readText()})
    val bones=metadata.getJSONArray("bones").length()
    val bytes=context.assets.open("companion3d/model.bin").use{it.readBytes()}
    val vertices=ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).apply{put(bytes);position(0)}
    val animation:FloatArray
    val clips:Map<String,RigClip>
    init {
        require(metadata.getInt("format")==1 && metadata.getInt("stride")==64 && bones in 1..24 && bytes.size%192==0)
        val raw=context.assets.open("companion3d/animation.bin").use{it.readBytes()}
        val floats=ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()
        animation=FloatArray(floats.remaining()).also{floats.get(it)}
        val list=metadata.getJSONArray("clips")
        clips=(0 until list.length()).map { i-> val c=list.getJSONObject(i)
            RigClip(c.getString("name"),c.getInt("frames"),c.getInt("fps"),c.getInt("offset"),c.getBoolean("loop"))
        }.associateBy{it.name}
        require(clips.keys.containsAll(listOf("Idle","Wave","Walk","Jump")))
        require(clips.values.all{it.frames>1 && it.fps>0 && it.offset>=0 && it.offset+it.frames*bones*16<=animation.size})
    }
}
private object RigCache {
    @Volatile private var data:RigData?=null
    @Synchronized fun get(context:Context):RigData=data?:RigData(context.applicationContext).also{data=it}
}

private class RiggedRenderer(private val context:Context,private val failure:()->Unit):GLSurfaceView.Renderer {
    @Volatile var moving=true
    @Volatile var yaw=0f
    @Volatile private var requested="Idle"
    @Volatile private var generation=0
    fun play(name:String){requested=name;generation++}
    private var consumed=0;private var clipName="Idle";private var time=0f;private var last=0L
    private var ready=false;private var program=0;private lateinit var data:RigData
    private val projection=FloatArray(16);private val view=FloatArray(16);private val model=FloatArray(16);private val vp=FloatArray(16);private val mvp=FloatArray(16)
    private val skin=FloatArray(24*16)
    private var boneLoc=0;private var mvpLoc=0;private var modelLoc=0
    override fun onSurfaceCreated(gl:GL10?,config:EGLConfig?) {
        ready=false
        try {
            data=RigCache.get(context)
            val limit=IntArray(1);glGetIntegerv(GL_MAX_VERTEX_UNIFORM_VECTORS,limit,0);require(limit[0]>=112)
            val vertex=shader(GL_VERTEX_SHADER,"""
                attribute vec3 aPosition; attribute vec3 aNormal; attribute vec2 aUv;
                attribute vec4 aJoints; attribute vec4 aWeights;
                uniform mat4 uBones[24]; uniform mat4 uMvp; uniform mat4 uModel;
                varying vec2 vUv; varying vec3 vNormal;
                void main(){
                  mat4 skin=uBones[int(aJoints.x)]*aWeights.x+uBones[int(aJoints.y)]*aWeights.y+uBones[int(aJoints.z)]*aWeights.z+uBones[int(aJoints.w)]*aWeights.w;
                  gl_Position=uMvp*skin*vec4(aPosition,1.0);vNormal=mat3(uModel)*mat3(skin)*aNormal;vUv=aUv;
                }
            """.trimIndent())
            val fragment=shader(GL_FRAGMENT_SHADER,"""
                precision mediump float;uniform sampler2D uTexture;varying vec2 vUv;varying vec3 vNormal;
                void main(){vec3 n=normalize(vNormal);float light=.85+.15*max(dot(n,normalize(vec3(-.4,.7,1.0))),0.0);gl_FragColor=vec4(texture2D(uTexture,vUv).rgb*light,1.0);}
            """.trimIndent())
            program=glCreateProgram();glAttachShader(program,vertex);glAttachShader(program,fragment)
            listOf("aPosition","aNormal","aUv","aJoints","aWeights").forEachIndexed{i,n->glBindAttribLocation(program,i,n)}
            glLinkProgram(program);val status=IntArray(1);glGetProgramiv(program,GL_LINK_STATUS,status,0);check(status[0]!=0){glGetProgramInfoLog(program)}
            glDeleteShader(vertex);glDeleteShader(fragment);glUseProgram(program)
            boneLoc=glGetUniformLocation(program,"uBones");mvpLoc=glGetUniformLocation(program,"uMvp");modelLoc=glGetUniformLocation(program,"uModel")
            val buffer=IntArray(1);glGenBuffers(1,buffer,0);glBindBuffer(GL_ARRAY_BUFFER,buffer[0]);data.vertices.position(0);glBufferData(GL_ARRAY_BUFFER,data.bytes.size,data.vertices,GL_STATIC_DRAW)
            for(i in 0..4)glEnableVertexAttribArray(i)
            glVertexAttribPointer(0,3,GL_FLOAT,false,64,0);glVertexAttribPointer(1,3,GL_FLOAT,false,64,12);glVertexAttribPointer(2,2,GL_FLOAT,false,64,24);glVertexAttribPointer(3,4,GL_FLOAT,false,64,32);glVertexAttribPointer(4,4,GL_FLOAT,false,64,48)
            val texture=IntArray(1);glGenTextures(1,texture,0);glActiveTexture(GL_TEXTURE0);glBindTexture(GL_TEXTURE_2D,texture[0])
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE)
            val bitmap=context.assets.open("companion3d/albedo.png").use{BitmapFactory.decodeStream(it)}
            // Android bitmap rows start at the top; Blender atlas V starts at the bottom.
            val flip=android.graphics.Matrix().apply{preScale(1f,-1f)}
            val flipped=android.graphics.Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,flip,false)
            GLUtils.texImage2D(GL_TEXTURE_2D,0,flipped,0);if(flipped!==bitmap)flipped.recycle();bitmap.recycle()
            glUniform1i(glGetUniformLocation(program,"uTexture"),0);glEnable(GL_DEPTH_TEST);glDisable(GL_CULL_FACE);glClearColor(1f,.965f,.902f,1f)
            Matrix.setLookAtM(view,0,0f,1.4f,7f,0f,1.25f,0f,0f,1f,0f)
            last=0;ready=true
        } catch(_:Exception){failure()}
    }
    override fun onSurfaceChanged(gl:GL10?,width:Int,height:Int) {
        glViewport(0,0,width,height)
        val aspect=width.toFloat()/height.coerceAtLeast(1);val halfHeight=maxOf(1.55f,1.6f/aspect)
        Matrix.orthoM(projection,0,-halfHeight*aspect,halfHeight*aspect,-halfHeight,halfHeight,1f,15f)
        Matrix.multiplyMM(vp,0,projection,0,view,0)
    }
    override fun onDrawFrame(gl:GL10?) {
        if(!ready)return
        val now=SystemClock.uptimeMillis();val dt=if(last==0L)0f else ((now-last)/1000f).coerceAtMost(.1f);last=now
        if(consumed!=generation){consumed=generation;clipName=requested;time=0f}
        if(moving)time+=dt
        var clip=data.clips.getValue(clipName)
        val duration=(clip.frames-1).toFloat()/clip.fps
        if((!clip.loop && time>=duration)||(clipName=="Walk" && time>=4f)) {clipName="Idle";time=0f;clip=data.clips.getValue("Idle")}
        val frame=rigFrame(time,clip.fps,clip.frames,clip.loop)
        val a=frame.first;val b=frame.second;val blend=frame.third
        for(i in 0 until data.bones*16) {
            val start=clip.offset+i;skin[i]=data.animation[start+a*data.bones*16]*(1-blend)+data.animation[start+b*data.bones*16]*blend
        }
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT);glUseProgram(program)
        Matrix.setIdentityM(model,0);Matrix.rotateM(model,0,yaw%360f,0f,1f,0f)
        Matrix.multiplyMM(mvp,0,vp,0,model,0)
        glUniformMatrix4fv(boneLoc,data.bones,false,skin,0);glUniformMatrix4fv(mvpLoc,1,false,mvp,0);glUniformMatrix4fv(modelLoc,1,false,model,0)
        glDrawArrays(GL_TRIANGLES,0,data.bytes.size/64)
    }
    private fun shader(type:Int,source:String):Int {
        val id=glCreateShader(type);glShaderSource(id,source);glCompileShader(id);val status=IntArray(1);glGetShaderiv(id,GL_COMPILE_STATUS,status,0);check(status[0]!=0){glGetShaderInfoLog(id)};return id
    }
}
