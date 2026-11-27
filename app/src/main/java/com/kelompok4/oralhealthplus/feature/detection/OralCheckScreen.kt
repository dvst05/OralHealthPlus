package com.kelompok4.oralhealthplus.feature.detection

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kelompok4.oralhealthplus.ui.components.GradientFilledButton
import com.kelompok4.oralhealthplus.ui.components.ScreenHeader
import com.kelompok4.oralhealthplus.ui.theme.GreenSoft
import com.kelompok4.oralhealthplus.ui.theme.GreenSuccess

/** Layar "Cek Mulut": kondisi kosong (belum ada foto) dan pratinjau foto siap analisis. */
@Composable
fun OralCheckScreen(
    foto: Bitmap?,
    pesanGalat: String?,
    onBack: () -> Unit,
    onTakePhoto: () -> Unit,
    onGallery: () -> Unit,
    onChangePhoto: () -> Unit,
    onAnalyze: () -> Unit,
    isGuest: Boolean = false,
    remainingGuestAnalyses: Int = 1
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        ScreenHeader(title = "Cek Mulut", onBack = onBack)

        if (isGuest) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (remainingGuestAnalyses > 0) {
                        "Mode Tamu: Tersisa $remainingGuestAnalyses kesempatan scan & analisis gratis. Masuk untuk akses tanpa batas."
                    } else {
                        "Kuota scan & analisis tamu telah habis. Silakan masuk akun untuk analisis lebih lanjut."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            if (foto == null) {
                KondisiKosong(pesanGalat, onTakePhoto, onGallery)
            } else {
                FotoSiap(foto, onChangePhoto)
            }
        }

        GradientFilledButton(
            onClick = onAnalyze,
            enabled = foto != null,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .height(48.dp)
        ) {
            Text("Analisis Foto", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun KondisiKosong(pesanGalat: String?, onTakePhoto: () -> Unit, onGallery: () -> Unit) {
    val dashColor = MaterialTheme.colorScheme.secondary
    Box(
        Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .drawBehind {
                drawRoundRect(
                    color = dashColor,
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(Modifier.padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                Alignment.Center
            ) {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Belum ada foto",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Ambil foto area mulut atau pilih dari galeri untuk memulai pemeriksaan.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }

    Card(
        Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Tips agar hasil deteksi akurat",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            listOf(
                "Gunakan pencahayaan yang terang dan merata",
                "Buka mulut cukup lebar dan tahan kamera agar tidak goyang",
                "Fokuskan kamera pada area gigi dan gusi"
            ).forEachIndexed { i, tip ->
                Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        Alignment.Center
                    ) {
                        Text(
                            "${i + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        tip,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    Row(Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        GradientFilledButton(
            onClick = onTakePhoto,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Kamera", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp))
        }
        OutlinedButton(
            onClick = onGallery,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Icon(
                Icons.Outlined.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Galeri",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (pesanGalat != null) {
        Text(
            pesanGalat,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun FotoSiap(foto: Bitmap, onChangePhoto: () -> Unit) {
    val gambar = remember(foto) { foto.asImageBitmap() }
    Box(
        Modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Image(gambar, "Foto mulut terpilih", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Row(
            Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(GreenSoft)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Foto siap dianalisis", style = MaterialTheme.typography.labelMedium, color = GreenSuccess)
        }
    }
    Text(
        "Pastikan gigi dan gusi terlihat jelas. Jika foto buram atau gelap, silakan ganti foto terlebih dahulu.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
    )
    OutlinedButton(
        onClick = onChangePhoto,
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.height(44.dp)
    ) {
        Icon(Icons.Filled.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Ganti Foto", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
    }
}