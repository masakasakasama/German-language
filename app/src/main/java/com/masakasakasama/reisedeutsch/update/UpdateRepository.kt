package com.masakasakasama.reisedeutsch.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class ReleaseInfo(val versionCode:Int,val versionName:String,val packageName:String,val apkUrl:String,val sha256:String,val sizeBytes:Long,val notes:String)
class UpdateException(message:String,cause:Throwable?=null):Exception(message,cause)

object UpdateRepository{
    private const val owner="masakasakasama"
    private const val repo="German-language"
    private const val maxApkBytes=250L*1024*1024
    private fun connection(url:String):HttpURLConnection{
        val c=(URL(url).openConnection() as HttpURLConnection);c.instanceFollowRedirects=true;c.connectTimeout=15_000;c.readTimeout=20_000;c.setRequestProperty("User-Agent","ReiseDeutsch-Android");c.setRequestProperty("Accept","application/json")
        return c
    }
    suspend fun check(installedVersion:Int):ReleaseInfo?=withContext(Dispatchers.IO){
        val latest=connection("https://github.com/$owner/$repo/releases/latest")
        try{
            if(latest.responseCode !in 200..399)throw UpdateException("リリース情報を取得できませんでした (${latest.responseCode})")
            val uri=Uri.parse(latest.url.toString());val segments=uri.pathSegments
            if(!uri.host.equals("github.com",true)||segments.size<5||segments[0]!=owner||segments[1]!=repo||segments[2]!="releases"||segments[3]!="tag")throw UpdateException("配布元を確認できませんでした")
            val tag=segments[4];if(!tag.matches(Regex("v[0-9]+")))throw UpdateException("アプリの版数が正しくありません")
            val version=tag.removePrefix("v").toIntOrNull()?:throw UpdateException("アプリの版数が正しくありません")
            if(version<=installedVersion)return@withContext null
            val feedUrl="https://github.com/$owner/$repo/releases/download/$tag/latest.json"
            val feed=connection(feedUrl)
            try{
                if(feed.responseCode !in 200..299)throw UpdateException("更新情報がまだ公開されていません")
                val body=feed.inputStream.use{input->val out=java.io.ByteArrayOutputStream();val buffer=ByteArray(8_192);while(out.size()<=65_536){val n=input.read(buffer,0,minOf(buffer.size,65_537-out.size()));if(n<0)break;out.write(buffer,0,n)};out.toByteArray()};if(body.size>65_536)throw UpdateException("更新情報が大きすぎます")
                val j=JSONObject(body.toString(Charsets.UTF_8))
                if(j.optInt("schemaVersion")!=1||j.optString("packageName")!="com.masakasakasama.reisedeutsch")throw UpdateException("このアプリ向けの更新ではありません")
                val code=j.getInt("versionCode");if(code!=version)throw UpdateException("公開タグとアプリ版数が一致しません")
                val apk=j.getString("apkUrl");val apkUri=Uri.parse(apk)
                if(apkUri.scheme?.equals("https",true)!=true||apkUri.host?.equals("github.com",true)!=true||!apkUri.path.orEmpty().startsWith("/$owner/$repo/releases/download/$tag/ReiseDeutsch-"))throw UpdateException("APKの配布先を確認できませんでした")
                val hash=j.getString("sha256").lowercase();if(!hash.matches(Regex("[0-9a-f]{64}")))throw UpdateException("APKの検証情報が正しくありません")
                val size=j.optLong("sizeBytes",0);if(size !in 1..maxApkBytes)throw UpdateException("APKのサイズを確認できません")
                ReleaseInfo(code,j.getString("versionName"),j.getString("packageName"),apk,hash,size,j.optString("notes"))
            }finally{feed.disconnect()}
        }finally{latest.disconnect()}
    }

    suspend fun downloadAndPrepare(context:Context,release:ReleaseInfo):File=withContext(Dispatchers.IO){
        val dir=File(context.cacheDir,"updates");if(!dir.exists()&&!dir.mkdirs())throw UpdateException("更新用領域を作れませんでした")
        val temp=File(dir,"reisedeutsch-${release.versionCode}.part");val target=File(dir,"reisedeutsch-${release.versionCode}.apk")
        try{
            val c=connection(release.apkUrl)
            try{
                if(c.responseCode !in 200..299)throw UpdateException("APKを取得できませんでした (${c.responseCode})")
                val length=c.contentLengthLong;if(length>0&&length!=release.sizeBytes)throw UpdateException("APKのサイズが更新情報と一致しません")
                c.inputStream.use{input->temp.outputStream().buffered().use{output->val buf=ByteArray(64*1024);var total=0L;while(true){val n=input.read(buf);if(n<0)break;total+=n;if(total>maxApkBytes||total>release.sizeBytes)throw UpdateException("APKのサイズ上限を超えました");output.write(buf,0,n)}}}
            }finally{c.disconnect()}
            if(temp.length()!=release.sizeBytes)throw UpdateException("APKのダウンロードが途中で終わりました")
            val md=MessageDigest.getInstance("SHA-256");temp.inputStream().buffered().use{stream->val buffer=ByteArray(64*1024);while(true){val n=stream.read(buffer);if(n<0)break;md.update(buffer,0,n)}};val digest=md.digest().joinToString(""){"%02x".format(it)}
            if(digest!=release.sha256)throw UpdateException("APKのSHA-256検証に失敗しました")
            verifyApk(context,temp,release)
            if(target.exists())target.delete();if(!temp.renameTo(target))throw UpdateException("検証済みAPKを保存できませんでした")
            target
        }catch(e:Exception){temp.delete();throw e}
    }

    @Suppress("DEPRECATION")
    private fun verifyApk(context:Context,file:File,release:ReleaseInfo){
        val pm=context.packageManager
        val signingFlags=if(Build.VERSION.SDK_INT>=28)PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val archive=(if(Build.VERSION.SDK_INT>=33)pm.getPackageArchiveInfo(file.path,PackageManager.PackageInfoFlags.of(signingFlags.toLong())) else pm.getPackageArchiveInfo(file.path,signingFlags))
            ?:throw UpdateException("APKとして読み取れません")
        if(archive.packageName!=release.packageName)throw UpdateException("異なるアプリのAPKです")
        val code=if(Build.VERSION.SDK_INT>=28)archive.longVersionCode.toInt() else archive.versionCode
        if(code!=release.versionCode)throw UpdateException("APKの版数が一致しません")
        val installed=if(Build.VERSION.SDK_INT>=33)pm.getPackageInfo(context.packageName,PackageManager.PackageInfoFlags.of(signingFlags.toLong())) else pm.getPackageInfo(context.packageName,signingFlags)
        val installedSigners=if(Build.VERSION.SDK_INT>=28)installed.signingInfo?.apkContentsSigners else installed.signatures
        val archivedSigners=if(Build.VERSION.SDK_INT>=28)archive.signingInfo?.apkContentsSigners else archive.signatures
        val expected=installedSigners.orEmpty().map{MessageDigest.getInstance("SHA-256").digest(it.toByteArray()).joinToString(""){b->"%02x".format(b)}}.toSet()
        val actual=archivedSigners.orEmpty().map{MessageDigest.getInstance("SHA-256").digest(it.toByteArray()).joinToString(""){b->"%02x".format(b)}}.toSet()
        if(expected.isEmpty()||actual!=expected)throw UpdateException("APKの署名がインストール済みアプリと異なります")
    }

    fun installerIntent(context:Context,file:File):Intent{
        val uri=FileProvider.getUriForFile(context,"${context.packageName}.files",file)
        return Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
