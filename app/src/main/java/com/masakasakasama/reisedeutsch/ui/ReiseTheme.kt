package com.masakasakasama.reisedeutsch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.masakasakasama.designsystem.generated.TatsuAccent
import com.masakasakasama.designsystem.TatsuTheme

data class ReiseColors(val paper:Color,val surface:Color,val ink:Color,val muted:Color,val action:Color,val onAction:Color,val actionSoft:Color,val speech:Color,val onSpeech:Color,val progress:Color,val mapPaper:Color,val line:Color)
private val Light=ReiseColors(Color(0xFFFFF8EE),Color.White,Color(0xFF17384B),Color(0xFF526574),Color(0xFFBA363B),Color.White,Color(0xFFFFE4DF),Color(0xFF176D9A),Color.White,Color(0xFF227C65),Color(0xFFF4E8BC),Color(0xFFD7DCE2))
private val Dark=ReiseColors(Color(0xFF171E26),Color(0xFF24313D),Color(0xFFF1F5F8),Color(0xFFB7C5CF),Color(0xFFFFB4B8),Color(0xFF4A1018),Color(0xFF532A31),Color(0xFF235A79),Color(0xFFF1F5F8),Color(0xFF86D7BC),Color(0xFF2B332D),Color(0xFF455461))
val LocalReiseColors=staticCompositionLocalOf{Light}
@Composable fun ReiseTheme(dark:Boolean, content: @Composable () -> Unit){
    TatsuTheme(accent=TatsuAccent.Ocean,darkTheme=dark){
        val c=if(dark)Dark else Light
        MaterialTheme(colorScheme=MaterialTheme.colorScheme.copy(primary=c.action,onPrimary=c.onAction,background=c.paper,surface=c.surface,onSurface=c.ink,onBackground=c.ink,secondary=c.speech,onSecondary=c.onSpeech,error=Color(0xFFB3261E)),typography=Typography(bodyLarge=MaterialTheme.typography.bodyLarge.copy(color=c.ink)),content={CompositionLocalProvider(LocalReiseColors provides c,content=content)})
    }
}
