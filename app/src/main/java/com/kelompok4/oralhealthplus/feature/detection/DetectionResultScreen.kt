package com.kelompok4.oralhealthplus.feature.detection

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kelompok4.oralhealthplus.data.model.DetectionResult
import com.kelompok4.oralhealthplus.data.model.DiseaseDescriptions
import com.kelompok4.oralhealthplus.data.model.Urgensi
import com.kelompok4.oralhealthplus.ui.components.GradientFilledButton
import com.kelompok4.oralhealthplus.ui.components.LabelChip
import com.kelompok4.oralhealthplus.ui.components.PillButton
import com.kelompok4.oralhealthplus.ui.components.ScreenHeader
import com.kelompok4.oralhealthplus.ui.theme.AmberSoft
import com.kelompok4.oralhealthplus.ui.theme.AmberText
import com.kelompok4.oralhealthplus.ui.theme.GreenSoft
import com.kelompok4.oralhealthplus.ui.theme.GreenSuccess
import com.kelompok4.oralhealthplus.ui.theme.RedError
import com.kelompok4.oralhealthplus.ui.theme.RedSoft

data class EduItem(val jenis: Jenis, val judul: String, val url: String? = null) {
    enum class Jenis(val label: String) { ARTIKEL("Artikel"), VIDEO("Video") }
}

private data class UrgensiUi(val label: String, val bg: Color, val fg: Color, val ikon: ImageVector)

private fun Urgensi.ui() = when (this) {
    Urgensi.EDUKASI_MANDIRI -> UrgensiUi("Edukasi Mandiri", GreenSoft, GreenSuccess, Icons.Filled.Check)
    Urgensi.KONTROL_RUTIN -> UrgensiUi("Kontrol Rutin", AmberSoft, AmberText, Icons.Filled.Schedule)
    Urgensi.SEGERA_KE_DOKTER -> UrgensiUi("Segera ke Dokter", RedSoft, RedError, Icons.Filled.Warning)
}

/** Layar "Hasil Deteksi" selaras dengan tema dan komponen bersama aplikasi. */
@Composable
fun DetectionResultScreen(
    hasil: DetectionResult,
    foto: Bitmap?,
    rekomendasi: List<EduItem>,
    onTryAgain: () -> Unit,
    onFinish: () -> Unit,
    onSeeAll: () -> Unit,
    onItemClick: (EduItem) -> Unit,
    onTanyaChatbot: () -> Unit,
    onCariKlinik: () -> Unit,
    onHubungiDokter: () -> Unit = {},
    riwayatTersimpan: Boolean = true
) {
    val urgensi = hasil.urgensi ?: Urgensi.KONTROL_RUTIN
    val ui = urgensi.ui()
    val namaPenyakit = hasil.kelas.replace('_', ' ')

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        ScreenHeader(title = "Hasil Deteksi", onBack = null)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            if (foto != null) {
                val gambar = remember(foto) { foto.asImageBitmap() }
                Image(
                    gambar,
                    contentDescription = "Foto mulut yang dianalisis",
                    modifier = Modifier
                        .size(width = 200.dp, height = 260.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = namaPenyakit,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 14.dp, bottom = 10.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                LabelChip("Keyakinan ${(hasil.confidence * 100).toInt()}%")
                Row(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ui.bg)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(ui.ikon, contentDescription = null, tint = ui.fg, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(ui.label, style = MaterialTheme.typography.labelMedium, color = ui.fg)
                }
            }

            Text(
                text = DiseaseDescriptions.getDescription(hasil.kelas),
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 19.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            Text(
                text = "Catatan: Hasil ini merupakan skrining awal dan bukan pengganti diagnosis medis. Silakan konsultasikan dengan dokter gigi untuk pemeriksaan klinis lebih lanjut.",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 15.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )

            if (!riwayatTersimpan) {
                Text(
                    text = "Hasil ini belum berhasil disimpan ke riwayat pemeriksaan.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            if (urgensi == Urgensi.SEGERA_KE_DOKTER) {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .clickable(onClick = onHubungiDokter),
                    colors = CardDefaults.cardColors(containerColor = GreenSoft),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Hubungi Dokter Gigi via WhatsApp",
                        style = MaterialTheme.typography.titleSmall,
                        color = GreenSuccess,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clickable(onClick = onCariKlinik),
                    colors = CardDefaults.cardColors(containerColor = ui.bg),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Cari Klinik Gigi Terdekat",
                        style = MaterialTheme.typography.titleSmall,
                        color = ui.fg,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (rekomendasi.isNotEmpty()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pelajari Tentang $namaPenyakit",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onSeeAll) {
                        Text(
                            text = "Lihat Semua",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                rekomendasi.take(2).forEach { EduCard(it) { onItemClick(it) } }
            } else {
                OutlinedButton(
                    onClick = onSeeAll,
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .height(46.dp)
                ) {
                    Text(
                        text = "Lihat Konten Edukasi Terkait",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            TextButton(onClick = onTanyaChatbot, modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "Tanya Chatbot Tentang Hasil Ini",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PillButton(
                text = "Ulangi Foto",
                onClick = onTryAgain,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primary
            )
            PillButton(
                text = "Selesai",
                onClick = onFinish,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun EduCard(item: EduItem, onClick: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                Alignment.Center
            ) {
                Icon(
                    if (item.jenis == EduItem.Jenis.VIDEO) Icons.Filled.PlayCircle else Icons.Filled.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                LabelChip(item.jenis.label)
                Text(
                    text = item.judul,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
fun RejectedContent(confidence: Float, onTryAgain: () -> Unit, onBack: () -> Unit) {
    PesanLayar(
        judul = "Hasil Foto Kurang Meyakinkan",
        isi = "Tingkat keyakinan hanya ${(confidence * 100).toInt()}%. Silakan ambil ulang foto dengan pencahayaan yang cukup, mulut terbuka lebar, dan gigi serta gusi terlihat fokus.",
        tombol = "Ulangi Foto",
        onTombol = onTryAgain,
        onBack = onBack
    )
}

@Composable
fun ErrorContent(pesan: String, onTryAgain: () -> Unit, onBack: () -> Unit) {
    PesanLayar("Terjadi Kesalahan", pesan, "Coba Lagi", onTryAgain, onBack)
}

@Composable
private fun PesanLayar(judul: String, isi: String, tombol: String, onTombol: () -> Unit, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        ScreenHeader(title = "Cek Mulut", onBack = onBack)
        Column(
            Modifier
                .weight(1f)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AmberSoft)
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = AmberText, modifier = Modifier.size(32.dp))
                    Text(
                        text = judul,
                        style = MaterialTheme.typography.titleMedium,
                        color = AmberText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = isi,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AmberText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
        GradientFilledButton(
            onClick = onTombol,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .height(48.dp)
        ) {
            Text(tombol, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun LoadingContent() {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Text(
            text = "Menganalisis foto Anda…",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}