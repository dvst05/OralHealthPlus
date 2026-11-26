package com.kelompok4.oralhealthplus.feature.detection

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Layar "Oral Check": kondisi kosong (belum ada foto) dan "Photo ready for analysis". */
@Composable
fun OralCheckScreen(
    foto: Bitmap?,
    pesanGalat: String?,
    onBack: () -> Unit,
    onTakePhoto: () -> Unit,
    onGallery: () -> Unit,
    onChangePhoto: () -> Unit,
    onAnalyze: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        OralHeader("Oral Check", onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            if (foto == null) KondisiKosong(pesanGalat, onTakePhoto, onGallery)
            else FotoSiap(foto, onChangePhoto)
        }
        GradientButton(
            "Analyze", onAnalyze, enabled = foto != null,
            modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                .padding(start = 56.dp, end = 56.dp, top = 8.dp, bottom = 24.dp)
        )
    }
}

@Composable
private fun KondisiKosong(pesanGalat: String?, onTakePhoto: () -> Unit, onGallery: () -> Unit) {
    val garis = OralColors.DashBorder
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(262.dp)
            .clip(RoundedCornerShape(28.dp)).background(OralColors.SoftBg)
            .drawBehind {   // Compose tidak punya border putus-putus bawaan
                drawRoundRect(
                    color = garis, cornerRadius = CornerRadius(28.dp.toPx()),
                    style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(Modifier.padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(68.dp).clip(CircleShape).background(OralColors.IconCircle), Alignment.Center) {
                Icon(Icons.Filled.CameraAlt, null, tint = OralColors.Primary, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text("No photo yet", color = OralColors.Primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Take a new photo or select from the gallery to start the check.",
                color = OralColors.Primary, fontSize = 13.sp, textAlign = TextAlign.Center
            )
        }
    }

    Card(
        Modifier.fillMaxWidth().padding(top = 22.dp), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Tips for accurate results", color = OralColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            listOf(
                "Use sufficient light",
                "Open mouth wide and keep the camera steady",
                "Focus on your teeth and gums"
            ).forEachIndexed { i, tip ->
                Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(28.dp).clip(CircleShape).background(OralColors.SoftBg), Alignment.Center) {
                        Text("${i + 1}", color = OralColors.Primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(tip, color = OralColors.Primary, fontSize = 12.5.sp)
                }
            }
        }
    }

    Row(Modifier.padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        GradientButton("Take Photo", onTakePhoto, Modifier.weight(1f), icon = Icons.Filled.CameraAlt)
        OutlineButton("Gallery", onGallery, Modifier.weight(1f), icon = Icons.Outlined.Image)
    }
    if (pesanGalat != null) {
        Text(pesanGalat, color = OralColors.ErrorText, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun FotoSiap(foto: Bitmap, onChangePhoto: () -> Unit) {
    val gambar = remember(foto) { foto.asImageBitmap() }
    Box(Modifier.fillMaxWidth().height(376.dp).clip(RoundedCornerShape(8.dp))) {
        Image(gambar, "Selected oral photo", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        StatusChip(
            "Photo ready for analysis", Color(0xFFF0FFF4), OralColors.Success,
            Modifier.align(Alignment.TopStart).padding(12.dp), Icons.Filled.Check
        )
    }
    Text(
        "Ensure teeth and gums are clearly visible. If the photo is blurry or dark, please replace it first.",
        color = OralColors.Primary, fontSize = 12.5.sp, textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    )
    GradientButton("Change Photo", onChangePhoto, icon = Icons.Filled.Refresh)
}
