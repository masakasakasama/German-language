package com.masakasakasama.reisedeutsch.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masakasakasama.reisedeutsch.data.PhraseCard
import com.masakasakasama.designsystem.TatsuCard
import com.masakasakasama.designsystem.TatsuButton

@Composable fun ReiseCard(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit){val c=LocalReiseColors.current;Surface(modifier=modifier,shape=RoundedCornerShape(24.dp),color=c.surface,contentColor=c.ink,border=BorderStroke(1.dp,c.line),shadowElevation=2.dp){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)}}
@Composable fun PrimaryButton(text:String,onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true){val c=LocalReiseColors.current;Button(onClick=onClick,modifier=modifier.heightIn(min=54.dp),enabled=enabled,shape=RoundedCornerShape(20.dp),colors=ButtonDefaults.buttonColors(containerColor=c.action,contentColor=c.onAction,disabledContainerColor=c.line)){Text(text,fontWeight=FontWeight.Bold)}}
@Composable fun PhraseTile(card:PhraseCard,onClick:()->Unit,assessment:String?=null){val c=LocalReiseColors.current;ReiseCard(Modifier.fillMaxWidth().heightIn(min=112.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.Top){Text(sceneIcon(card.scene),fontSize=30.sp);Text(assessmentLabel(assessment),color=c.muted,fontSize=12.sp)};Text(card.german,fontSize=17.sp,fontWeight=FontWeight.Bold);Text(card.japanese,color=c.muted,fontSize=13.sp);TextButton(onClick=onClick){Text("フレーズを見る →",color=c.action)}}}
@Composable fun BunnyCard(title:String,body:String,modifier:Modifier=Modifier){val c=LocalReiseColors.current;ReiseCard(modifier.fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){TravelBunny(Modifier.size(64.dp));Column(verticalArrangement=Arrangement.spacedBy(4.dp)){Text(title,fontWeight=FontWeight.Bold,color=c.ink);Text(body,color=c.muted,style=MaterialTheme.typography.bodyMedium)}}}}
@Composable fun ProgressBar(done:Int,total:Int){val c=LocalReiseColors.current;Column(verticalArrangement=Arrangement.spacedBy(5.dp)){LinearProgressIndicator(progress={if(total<=0)0f else(done.toFloat()/total).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(9.dp)),color=c.progress,trackColor=c.line);Text("$done / $total",style=MaterialTheme.typography.labelSmall,color=c.muted)}}
@Composable fun EmptyMessage(icon:String,title:String,body:String){Column(Modifier.fillMaxWidth().padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)){if(icon=="companion")TravelBunny(Modifier.size(96.dp)) else Text(icon,fontSize=42.sp);Text(title,fontWeight=FontWeight.Bold);Text(body,color=LocalReiseColors.current.muted,textAlign=TextAlign.Center)}}
fun sceneIcon(scene:String)=when(scene){"cafe"->"☕";"station"->"🚆";"hotel"->"🛎️";"sightseeing"->"🏰";else->"🇩🇪"}
fun assessmentLabel(value:String?)=when(value){"KNOW"->"😊 わかる";"OK"->"😐 少しわかる";"DONT_KNOW"->"😕 わからない";else->"未評価"}

@Composable fun TravelBunny(modifier:Modifier=Modifier){ CompanionCharacter(modifier.size(72.dp)) }
