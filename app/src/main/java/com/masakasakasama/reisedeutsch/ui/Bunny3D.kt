package com.masakasakasama.reisedeutsch.ui

import android.animation.ValueAnimator
import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.sin

/** Local mesh only: no WebView, network, camera permission or external rendering engine. */
@Composable fun Bunny3DCard() {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var paused by remember { mutableStateOf(!ValueAnimator.areAnimatorsEnabled()) }
    var active by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    var failed by remember { mutableStateOf(false) }
    val view = remember(context) { BunnySurface(context) { failed = true } }
    DisposableEffect(view, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) { active = true; view.onResume() }
            if (event == Lifecycle.Event.ON_PAUSE) { active = false; view.onPause() }
        }
        lifecycle.addObserver(observer)
        if (active) view.onResume()
        onDispose { lifecycle.removeObserver(observer); view.onPause() }
    }
    LaunchedEffect(active, paused, failed) {
        view.renderer.moving = !paused && ValueAnimator.areAnimatorsEnabled()
        view.requestRender()
        while (active && !paused && !failed && ValueAnimator.areAnimatorsEnabled()) {
            view.requestRender()
            delay(33) // Cap at about 30 fps; no background rendering.
        }
    }
    ReiseCard(Modifier.fillMaxWidth()) {
        if (failed) {
            TravelBunny(Modifier.size(120.dp))
            Text("旅の相棒と、今日も一歩！")
        } else {
            AndroidView(factory = { view }, modifier = Modifier.fillMaxWidth().height(230.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TextButton(onClick = { view.renderer.wave(); view.requestRender() }, enabled = !paused) { Text("🐾 手を振って！") }
                TextButton(onClick = { paused = !paused }) { Text(if (paused) "動きを再開" else "動きを止める") }
            }
        }
    }
}

private class BunnySurface(context: Context, onFailure: () -> Unit) : GLSurfaceView(context) {
    val renderer = BunnyRenderer(context) { post { onFailure() } }
    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        contentDescription = "赤い帽子と青いコートの3Dうさぎ。下のボタンで手を振ります"
    }
}

private class BunnyRenderer(private val context: Context, private val failure: () -> Unit) : GLSurfaceView.Renderer {
    @Volatile var moving = true
    @Volatile private var waveAt = -10000L
    fun wave() { if (moving) waveAt = SystemClock.uptimeMillis() }
    private data class Part(val first: Int, val count: Int, val color: FloatArray, val wave: Boolean)
    private var parts = emptyList<Part>()
    private var program = 0
    private var buffer = 0
    private var ready = false
    private var mvpLocation = 0
    private var modelLocation = 0
    private var colorLocation = 0
    private val projection = FloatArray(16)
    private val camera = FloatArray(16)
    private val vp = FloatArray(16)
    private val model = FloatArray(16)
    private val arm = FloatArray(16)
    private val mvp = FloatArray(16)
    private var elapsed = 0f
    private var lastFrame = 0L
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        ready = false
        try {
            val bytes = context.assets.open("mascot/bunny.bin").use { it.readBytes() }
            val scene = JSONObject(context.assets.open("mascot/bunny.json").bufferedReader().use { it.readText() })
            require(scene.getInt("format") == 1 && bytes.size % 24 == 0)
            val array = scene.getJSONArray("parts")
            parts = (0 until array.length()).map { i ->
                val p = array.getJSONObject(i); val c = p.getJSONArray("color")
                Part(p.getInt("first"), p.getInt("count"), FloatArray(4) { c.getDouble(it).toFloat() }, p.getBoolean("wave"))
            }
            require(parts.all { it.first >= 0 && it.count > 0 && (it.first + it.count) * 24 <= bytes.size })
            val vertex = shader(GL_VERTEX_SHADER, """
                attribute vec3 aPosition; attribute vec3 aNormal;
                uniform mat4 uMvp; uniform mat4 uModel; varying vec3 vNormal;
                void main(){ gl_Position=uMvp*vec4(aPosition,1.0); vNormal=mat3(uModel)*aNormal; }
            """.trimIndent())
            val fragment = shader(GL_FRAGMENT_SHADER, """
                precision mediump float; varying vec3 vNormal; uniform vec4 uColor;
                void main(){float light=0.83+0.17*max(dot(normalize(vNormal),normalize(vec3(-0.5,0.8,1.0))),0.0); gl_FragColor=vec4(uColor.rgb*light,1.0);}
            """.trimIndent())
            program = glCreateProgram(); glAttachShader(program, vertex); glAttachShader(program, fragment)
            glBindAttribLocation(program, 0, "aPosition"); glBindAttribLocation(program, 1, "aNormal"); glLinkProgram(program)
            val status = IntArray(1); glGetProgramiv(program, GL_LINK_STATUS, status, 0)
            check(status[0] != 0) { glGetProgramInfoLog(program) }
            glDeleteShader(vertex); glDeleteShader(fragment)
            mvpLocation=glGetUniformLocation(program,"uMvp"); modelLocation=glGetUniformLocation(program,"uModel"); colorLocation=glGetUniformLocation(program,"uColor")
            val ids=IntArray(1); glGenBuffers(1,ids,0); buffer=ids[0]; glBindBuffer(GL_ARRAY_BUFFER,buffer)
            val data=ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()); data.put(bytes).position(0)
            glBufferData(GL_ARRAY_BUFFER,bytes.size,data,GL_STATIC_DRAW)
            glEnable(GL_DEPTH_TEST)
            glClearColor(1f,.965f,.9f,1f)
            Matrix.setLookAtM(camera,0,0f,1.65f,7f,0f,1.5f,0f,0f,1f,0f)
            ready=true
        } catch (_: Exception) { failure() }
    }
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0,0,width,height)
        val aspect=width.toFloat()/height.coerceAtLeast(1)
        val halfHeight=maxOf(1.65f,1.45f/aspect)
        Matrix.orthoM(projection,0,-halfHeight*aspect,halfHeight*aspect,-halfHeight,halfHeight,1f,15f)
        Matrix.multiplyMM(vp,0,projection,0,camera,0)
    }
    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        if (!ready) return
        val now=SystemClock.uptimeMillis()
        if(moving && lastFrame!=0L) elapsed += ((now-lastFrame).coerceAtMost(100))/1000f
        lastFrame=now
        Matrix.setIdentityM(model,0)
        Matrix.translateM(model,0,0f,if(moving) .018f*(1f+sin(elapsed*3.14159f)) else 0f,0f)
        Matrix.rotateM(model,0,-12f,0f,1f,0f)
        System.arraycopy(model,0,arm,0,16)
        val waveTime=(now-waveAt)/1000f
        val angle=if(moving && waveTime in 0f..1.6f) 24f*sin(waveTime*12f)*sin(waveTime/1.6f*3.14159f) else 0f
        Matrix.translateM(arm,0,-.48f,.8784f,0f); Matrix.rotateM(arm,0,angle,0f,0f,1f); Matrix.translateM(arm,0,.48f,-.8784f,0f)
        glUseProgram(program); glBindBuffer(GL_ARRAY_BUFFER,buffer)
        glEnableVertexAttribArray(0); glEnableVertexAttribArray(1)
        glVertexAttribPointer(0,3,GL_FLOAT,false,24,0); glVertexAttribPointer(1,3,GL_FLOAT,false,24,12)
        for(part in parts) {
            val transform=if(part.wave) arm else model
            Matrix.multiplyMM(mvp,0,vp,0,transform,0)
            glUniformMatrix4fv(mvpLocation,1,false,mvp,0); glUniformMatrix4fv(modelLocation,1,false,transform,0)
            glUniform4fv(colorLocation,1,part.color,0); glDrawArrays(GL_TRIANGLES,part.first,part.count)
        }
    }
    private fun shader(type:Int, code:String):Int {
        val id=glCreateShader(type); glShaderSource(id,code); glCompileShader(id)
        val status=IntArray(1); glGetShaderiv(id,GL_COMPILE_STATUS,status,0)
        check(status[0]!=0) { glGetShaderInfoLog(id) }; return id
    }
}
