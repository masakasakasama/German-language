package com.masakasakasama.reisedeutsch.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masakasakasama.reisedeutsch.data.*

private fun roleName(role: GrammarRole) = when (role) {
    GrammarRole.SUBJECT -> "主語・だれが"
    GrammarRole.VERB -> "動詞"
    GrammarRole.OBJECT -> "何を・対象"
    GrammarRole.RECIPIENT -> "与格（3格）"
    GrammarRole.CONTEXT -> "時・場所・説明"
    GrammarRole.ARTICLE -> "冠詞"
    GrammarRole.NEGATION -> "否定・注意"
    GrammarRole.SOUND -> "音の組"
    GrammarRole.NOUN -> "名詞"
    GrammarRole.ENDING -> "活用の語尾"
    GrammarRole.LINKER -> "つなぐ語"
}

private fun roleColor(role: GrammarRole) = when (role) {
    GrammarRole.SUBJECT -> Color(0xFF3C80BE)
    GrammarRole.VERB -> Color(0xFFC14B54)
    GrammarRole.OBJECT -> Color(0xFFB27D1F)
    GrammarRole.RECIPIENT -> Color(0xFF278A6C)
    GrammarRole.CONTEXT -> Color(0xFF8F6BB7)
    GrammarRole.ARTICLE -> Color(0xFFAD712C)
    GrammarRole.NEGATION -> Color(0xFFAA4F85)
    GrammarRole.SOUND -> Color(0xFF278992)
    GrammarRole.NOUN -> Color(0xFFB27D1F)
    GrammarRole.ENDING -> Color(0xFF3C80BE)
    GrammarRole.LINKER -> Color(0xFF8F6BB7)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun GrammarDiagram(visual: GrammarVisual) {
    val c = LocalReiseColors.current
    var focus by remember(visual) { mutableStateOf<GrammarRole?>(null) }
    val roles = visual.rows.flatMap { it.tokens }.map { it.role }.distinct()
    ReiseCard(Modifier.fillMaxWidth()) {
        Text("🧩 ${visual.title}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("下のボタンで注目する箱を選べます。箱は左から右、折り返したら次の行へ読みます。", fontSize = 13.sp, color = c.muted)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            FilterChip(selected = focus == null, onClick = { focus = null }, label = { Text("全体") })
            roles.forEach { role ->
                FilterChip(selected = focus == role, onClick = { focus = if (focus == role) null else role },
                    label = { Text(roleName(role)) }, leadingIcon = { Text("●", color = roleColor(role)) })
            }
        }
        visual.rows.forEach { row ->
            Surface(color = c.paper, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(row.label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.tokens.forEachIndexed { index, token ->
                            val active = focus == null || focus == token.role
                            val accent = roleColor(token.role)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (index > 0) Text(token.connector, color = c.muted, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Surface(shape = RoundedCornerShape(14.dp), color = if (active) accent.copy(alpha = .13f) else c.surface,
                                    border = BorderStroke(if (focus == token.role) 2.dp else 1.dp, if (active) accent else c.line)) {
                                    Column(Modifier.widthIn(min = 68.dp, max = 220.dp).padding(horizontal = 10.dp, vertical = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Text(token.text, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = c.ink)
                                        Text(token.label, fontSize = 12.sp, lineHeight = 18.sp, color = c.muted)
                                    }
                                }
                            }
                        }
                    }
                    Text(row.meaning, fontSize = 14.sp, lineHeight = 22.sp, color = c.ink)
                }
            }
        }
        Text("💡 ${visual.takeaway}", fontWeight = FontWeight.SemiBold, lineHeight = 24.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun ExampleWordBoxes(example: GrammarExample) {
    val c = LocalReiseColors.current
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        example.parts.split('｜').filter { it.isNotBlank() }.forEach { part ->
            val de = part.substringBefore('＝').trim()
            val explanation = part.substringAfter('＝', "").trim()
            Surface(shape = RoundedCornerShape(14.dp), color = c.paper, border = BorderStroke(1.dp, c.line)) {
                Column(Modifier.widthIn(max = 240.dp).padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(de, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (explanation.isNotEmpty()) Text(explanation, fontSize = 13.sp, color = c.muted)
                }
            }
        }
    }
}
