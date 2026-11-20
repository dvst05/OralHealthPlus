package com.kelompok4.oralhealthplus.feature.detection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Token warna dari desain Figma. Setelah tema Compose Anggota 4 siap, ganti
 * OralColors.* dengan MaterialTheme.colorScheme.* / tipografi Poppins dari tema itu.
 * Dependensi: androidx.compose.material:material-icons-extended
 */
object OralColors {
    val Primary = Color(0xFF4A628A)
    val PrimaryEnd = Color(0xFF4B79A3)      // ujung gradien, digelapkan agar teks putih terbaca (>= 4.5:1)
    val SoftBg = Color(0xFFEEF3FA)
    val ChipBg = Color(0xFFE8F0FA)
    val IconCircle = Color(0xFFBFE0FA)
    val Line = Color(0xFF7BA7CC)
    val DashBorder = Color(0xFF8FA3BF)
    val Success = Color(0xFF1E7F38)
    val DisabledBg = Color(0xFFE5E5E5)
    val DisabledText = Color(0xFF9A9A9A)
    val ErrorText = Color(0xFFB3261E)
}

/** Judul di tengah + garis biru di bawahnya. `onBack` null = tanpa tombol kembali (layar Result). */
@Composable
fun OralHeader(title: String, onBack: (() -> Unit)?) {
    Column {
        Box(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OralColors.Primary)
                }
            }
            Text(title, color = OralColors.Primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(Modifier.padding(horizontal = 24.dp), thickness = 2.dp, color = OralColors.Line)
    }
}

@Composable
private fun PillContent(text: String, color: Color, icon: ImageVector?) {
    if (icon != null) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
    }
    Text(text, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun GradientButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, icon: ImageVector? = null
) {
    val bg = if (enabled) Brush.horizontalGradient(listOf(OralColors.Primary, OralColors.PrimaryEnd))
    else SolidColor(OralColors.DisabledBg)
    Row(
        modifier.height(48.dp).clip(RoundedCornerShape(50)).background(bg)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick).padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) { PillContent(text, if (enabled) Color.White else OralColors.DisabledText, icon) }
}

@Composable
fun OutlineButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(
        modifier.height(48.dp).clip(RoundedCornerShape(50))
            .border(BorderStroke(2.dp, OralColors.Primary), RoundedCornerShape(50))
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) { PillContent(text, OralColors.Primary, icon) }
}

@Composable
fun SolidButton(text: String, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.height(50.dp).clip(RoundedCornerShape(50)).background(color)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) { Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun StatusChip(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(
        modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
