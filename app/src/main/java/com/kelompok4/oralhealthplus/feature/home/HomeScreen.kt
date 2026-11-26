package com.kelompok4.oralhealthplus.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kelompok4.oralhealthplus.R
import com.kelompok4.oralhealthplus.feature.auth.ProfilePhotoStorage
import com.kelompok4.oralhealthplus.ui.components.GradientButton
import com.kelompok4.oralhealthplus.ui.theme.OralHealthPlusTheme

/**
 * Halaman utama (Figma Main Screen). Stateless: semua aksi diteruskan lewat callback ke NavHost (Anggota 3).
 *
 * Contoh di NavHost:
 *   composable("home") {
 *       HomeScreen(
 *           namaPengguna = session.username ?: "Pengguna",
 *           onScan = { navController.navigate("deteksi") },
 *           onClinic = { navController.navigate("klinik") },
 *           onVideos = { navController.navigate("edukasi?tab=1") },
 *           onArticles = { navController.navigate("edukasi?tab=0") },
 *           onReport = { navController.navigate("riwayat") },
 *           onChatbot = { navController.navigate("chatbot") },
 *           onToothpaste = null, // isi jika kelompok memutuskan fitur pasta gigi tetap ada
 *           onPengaturan = { navController.navigate("profil") },
 *       )
 *   }
 */
@Composable
fun HomeScreen(
    namaPengguna: String,
    onScan: () -> Unit,
    onClinic: () -> Unit,
    onVideos: () -> Unit,
    onArticles: () -> Unit,
    onReport: () -> Unit,
    onChatbot: () -> Unit,
    onPengaturan: () -> Unit,
    onToothpaste: (() -> Unit)? = null, // null = kartu pasta gigi disembunyikan
    onDoctors: (() -> Unit)? = null,
    onEncyclopedia: (() -> Unit)? = null,
    profilePhotoPath: String = ""
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(36.dp),
    ) {
        Header(namaPengguna, profilePhotoPath, onPengaturan)
        KartuScan(onScan)
        BagianLearn(onClinic, onVideos, onArticles, onReport)
        BagianMoreFeature(onChatbot, onToothpaste, onDoctors, onEncyclopedia)
    }
}

@Composable
private fun Header(namaPengguna: String, profilePhotoPath: String, onPengaturan: () -> Unit) {
    val avatarBitmap = remember(profilePhotoPath) {
        ProfilePhotoStorage.loadProfilePhotoBitmap(profilePhotoPath)
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (avatarBitmap != null) {
            Image(
                bitmap = avatarBitmap.asImageBitmap(),
                contentDescription = "Foto profil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onPengaturan),
            )
        } else {
            Image(
                painterResource(R.drawable.home_avatar),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onPengaturan),
            )
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text("Hi, Welcome!", style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp), color = MaterialTheme.colorScheme.primary)
            Text(
                namaPengguna, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onPengaturan) {
            Image(painterResource(R.drawable.ic_config), contentDescription = "Pengaturan akun", modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun KartuScan(onScan: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(painterResource(R.drawable.home_scan), contentDescription = null, modifier = Modifier.size(102.dp))
        Text(
            "Get Benefit from our Mouth-Scan Feature!",
            style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center,
        )
        Text(
            "You will get recommendation of your oral condition for your health and life.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center,
        )
        GradientButton("Scan Now", onClick = onScan, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun BagianLearn(onClinic: () -> Unit, onVideos: () -> Unit, onArticles: () -> Unit, onReport: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        JudulBagian("Learn")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val m = Modifier.weight(1f)
            MenuLearn(R.drawable.home_clinic, "Clinic", onClinic, m)
            MenuLearn(R.drawable.home_videos, "Videos", onVideos, m)
            MenuLearn(R.drawable.home_articles, "Articles", onArticles, m)
            MenuLearn(R.drawable.home_report, "Report", onReport, m)
        }
    }
}

@Composable
private fun MenuLearn(@DrawableRes ikon: Int, label: String, onClick: () -> Unit, modifier: Modifier) {
    val bentuk = RoundedCornerShape(8.dp)
    Column(
        modifier
            .height(91.dp)
            .clip(bentuk)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, bentuk)
            .clickable(onClickLabel = "Buka $label", onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(painterResource(ikon), contentDescription = null, modifier = Modifier.size(53.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, maxLines = 1)
    }
}

@Composable
private fun BagianMoreFeature(
    onChatbot: () -> Unit,
    onToothpaste: (() -> Unit)?,
    onDoctors: (() -> Unit)? = null,
    onEncyclopedia: (() -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        JudulBagian("More Feature")
        Text(
            "Introducing your personal oral health assistant. Our free app is here to guide you towards optimal oral health.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        KartuFitur(R.drawable.home_chatbot, "Chatbot Consultant", "Discuss Anything About Oral Disease", onChatbot)
        if (onDoctors != null) {
            KartuFitur(R.drawable.home_clinic, "Hubungi Dokter Gigi (WA)", "Pilih dokter gigi & konsultasi langsung via WhatsApp", onDoctors)
        }
        if (onEncyclopedia != null) {
            KartuFitur(R.drawable.home_articles, "Ensiklopedia Penyakit Mulut", "Pelajari gejala, penyebab & pencegahan penyakit gigi", onEncyclopedia)
        }
        if (onToothpaste != null) {
            KartuFitur(R.drawable.home_toothpaste, "Are you still using regular toothpaste?", "Choose the one that’s right for you", onToothpaste)
        }
    }
}

@Composable
private fun KartuFitur(@DrawableRes ikon: Int, judul: String, subjudul: String, onClick: () -> Unit) {
    val bentuk = RoundedCornerShape(8.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(bentuk)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, bentuk)
            .clickable(onClick = onClick)
            .padding(15.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(ikon), contentDescription = null, modifier = Modifier.size(width = 67.dp, height = 56.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(judul, style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp), color = MaterialTheme.colorScheme.primary)
            Text(subjudul, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun JudulBagian(teks: String) =
    Text(teks, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)

@Preview(name = "Home (dengan pasta gigi)", showBackground = true, widthDp = 400, heightDp = 1000)
@Composable
private fun PreviewHome() = OralHealthPlusTheme {
    HomeScreen("Puti", {}, {}, {}, {}, {}, {}, {}, onToothpaste = {})
}

@Preview(name = "Home (tanpa pasta gigi)", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun PreviewHomeTanpaPasta() = OralHealthPlusTheme {
    HomeScreen("Puti", {}, {}, {}, {}, {}, {}, {})
}
