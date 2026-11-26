package com.kelompok4.oralhealthplus.feature.detection

import android.graphics.Bitmap
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Satu baris rekomendasi edukasi. Diisi dari Fungsi 4 (Anggota 4); kosong = hanya tombol menuju layar edukasi. */
data class EduItem(val jenis: Jenis, val judul: String, val url: String? = null) {
    enum class Jenis(val label: String) { ARTIKEL("Article"), VIDEO("Video") }
}

private data class UrgensiUi(val label: String, val bg: Color, val fg: Color, val ikon: ImageVector)

private fun Urgensi.ui() = when (this) {
    Urgensi.EDUKASI_MANDIRI -> UrgensiUi("Self-care", Color(0xFFE8F5E9), Color(0xFF1E6B34), Icons.Filled.Check)
    Urgensi.KONTROL_RUTIN -> UrgensiUi("Routine Check-up", Color(0xFFFFF0CC), Color(0xFF6B4200), Icons.Filled.Schedule)
    Urgensi.SEGERA_KE_DOKTER -> UrgensiUi("See a Doctor ASAP", Color(0xFFFFEBEE), Color(0xFF9B1C1C), Icons.Filled.Warning)
}

/** SEMENTARA. Ganti dengan data ensiklopedia JSON dari Anggota 4. */
private fun deskripsi(kelas: String): String {
    val k = TriageEngine.normalisasi(kelas)
    return when {
        "gingivitis" in k ->
            "Gingivitis, or gum inflammation, is a condition that occurs due to inflammation of the gums, " +
                "characterized by swelling and redness around the base of the teeth. It can arise from the " +
                "accumulation of food debris on the teeth and gums, which then hardens and turns into plaque.\n\n" +
                "This condition should not be taken lightly. Left untreated, gingivitis can increase the risk of " +
                "further tooth and gum damage and can progress to periodontitis, a serious infection that can " +
                "damage the teeth and the bone surrounding them, potentially leading to tooth loss."
        "caries" in k ->
            "Dental caries (tooth decay) is damage to the tooth caused by acids produced by bacteria in plaque. " +
                "Early cavities may cause no pain, but left untreated they can deepen, cause toothache and " +
                "infection, and lead to tooth loss."
        "calculus" in k ->
            "Dental calculus (tartar) is hardened plaque that forms on teeth and along the gumline. It cannot be " +
                "removed by brushing and usually needs professional scaling. Left untreated it can irritate the " +
                "gums and contribute to gum disease."
        "ulcer" in k ->
            "Mouth ulcers (canker sores) are small, painful sores inside the mouth. Most heal on their own " +
                "within one to two weeks. See a dentist or doctor if an ulcer lasts about three weeks or longer, " +
                "keeps returning, or is unusually large or painful."
        "discoloration" in k ->
            "Tooth discoloration is a change in tooth color caused by staining from food, drinks or tobacco, " +
                "by aging or medication, or by damage inside the tooth. Good oral hygiene and professional " +
                "cleaning often help; a dentist can advise on the cause."
        "hypodontia" in k ->
            "Hypodontia is a developmental condition in which some permanent teeth never form. It can affect " +
                "chewing, speech and the spacing of teeth. A dentist can discuss ways to manage it."
        else -> "Please consult a dentist for a proper examination."
    }
}

/** Layar "Result" sesuai desain Figma. */
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
    val urgensi = (hasil.urgensi ?: Urgensi.KONTROL_RUTIN)
    val ui = urgensi.ui()
    Column(Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        OralHeader("Result", onBack = null)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            if (foto != null) {
                val gambar = remember(foto) { foto.asImageBitmap() }
                Image(
                    gambar, "Analyzed oral photo",
                    Modifier.size(width = 200.dp, height = 280.dp).clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                hasil.kelas.replace('_', ' '), color = OralColors.Primary, fontSize = 28.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp, bottom = 10.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusChip("Certainty ${(hasil.confidence * 100).toInt()}%", OralColors.ChipBg, OralColors.Primary)
                StatusChip(ui.label, ui.bg, ui.fg, icon = ui.ikon)
            }
            Text(
                deskripsi(hasil.kelas), color = OralColors.Primary, fontSize = 12.sp, lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp)
            )
            Text(
                "This is an initial screening, not a medical diagnosis. Please consult a dentist for a proper examination.",
                color = OralColors.Primary, fontSize = 11.sp, lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            )
            if (!riwayatTersimpan) {
                Text(
                    "This result couldn't be saved to your history.", color = OralColors.ErrorText,
                    fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
            if (urgensi == Urgensi.SEGERA_KE_DOKTER) {   // <<extend>> use case: klinik terdekat & WhatsApp dokter
                Card(
                    Modifier.fillMaxWidth().padding(top = 14.dp).clickable(onClick = onHubungiDokter),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "Hubungi Dokter Gigi via WhatsApp", color = Color(0xFF1E6B34), fontSize = 14.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp)
                    )
                }
                Card(
                    Modifier.fillMaxWidth().padding(top = 10.dp).clickable(onClick = onCariKlinik),
                    colors = CardDefaults.cardColors(containerColor = ui.bg), shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "Find a nearby dental clinic", color = ui.fg, fontSize = 14.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp)
                    )
                }
            }
            if (rekomendasi.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Learn About ${hasil.kelas.replace('_', ' ')}", color = OralColors.Primary,
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onSeeAll) {
                        Text("See All", color = OralColors.Primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                rekomendasi.take(2).forEach { EduCard(it) { onItemClick(it) } }
            } else {
                // Alur layar: Hasil Deteksi -> Rekomendasi Konten, juga saat kartu pratinjau belum diisi
                OutlineButton("See Educational Content", onSeeAll, Modifier.fillMaxWidth().padding(top = 18.dp))
            }
            TextButton(onClick = onTanyaChatbot, modifier = Modifier.padding(top = 8.dp)) {
                Text("Ask the chatbot about this result", color = OralColors.Primary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
        }
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SolidButton("Try Again", OralColors.Primary, onTryAgain, Modifier.weight(1f))
            SolidButton("Finish", OralColors.Success, onFinish, Modifier.weight(1f))
        }
    }
}

@Composable
private fun EduCard(item: EduItem, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(OralColors.SoftBg), Alignment.Center) {
                Icon(
                    if (item.jenis == EduItem.Jenis.VIDEO) Icons.Filled.PlayCircle else Icons.Filled.Description,
                    null, tint = OralColors.Primary, modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    item.jenis.label, color = OralColors.Primary, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(OralColors.ChipBg)
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                )
                Text(
                    item.judul, color = OralColors.Primary, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

/** Foto ditolak (confidence < ambang): minta ulangi foto. */
@Composable
fun RejectedContent(confidence: Float, onTryAgain: () -> Unit, onBack: () -> Unit) {
    PesanLayar(
        judul = "We're not sure about this photo",
        isi = "Certainty is only ${(confidence * 100).toInt()}%. Retake the photo with good light, mouth wide open, " +
            "and your teeth and gums in focus.",
        tombol = "Try Again", onTombol = onTryAgain, onBack = onBack
    )
}

@Composable
fun ErrorContent(pesan: String, onTryAgain: () -> Unit, onBack: () -> Unit) {
    PesanLayar("Something went wrong", pesan, "Try Again", onTryAgain, onBack)
}

@Composable
private fun PesanLayar(judul: String, isi: String, tombol: String, onTombol: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        OralHeader("Oral Check", onBack)
        Column(
            Modifier.weight(1f).padding(24.dp), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0CC))
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Warning, null, tint = Color(0xFF6B4200), modifier = Modifier.size(32.dp))
                    Text(
                        judul, color = Color(0xFF6B4200), fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        isi, color = Color(0xFF6B4200), fontSize = 13.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
        GradientButton(
            tombol, onTombol,
            Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 56.dp, end = 56.dp, bottom = 24.dp)
        )
    }
}

@Composable
fun LoadingContent() {
    Column(
        Modifier.fillMaxSize().background(Color.White), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = OralColors.Primary)
        Text(
            "Analyzing your photo…", color = OralColors.Primary, fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp)
        )
    }
}
