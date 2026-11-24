package com.kelompok4.oralhealthplus.feature.education

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kelompok4.oralhealthplus.ui.components.GradientButton
import com.kelompok4.oralhealthplus.ui.components.LabelChip
import com.kelompok4.oralhealthplus.ui.components.ScreenHeader
import com.kelompok4.oralhealthplus.ui.theme.OralHealthPlusTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pintu masuk untuk NavHost (Anggota 3). Contoh pemakaian:
 *   composable("edukasi?tab={tab}") {
 *       EducationRoute(
 *           ambilLabelTerakhir = { historyRepository.getLatestDetection()?.kelas },
 *           tabAwal = it.arguments?.getString("tab")?.toIntOrNull() ?: 0,
 *           onBack = { navController.popBackStack() },
 *           onCekMulut = { navController.navigate("deteksi") },
 *       )
 *   }
 */
@Composable
fun EducationRoute(
    ambilLabelTerakhir: suspend () -> String?,
    onBack: () -> Unit,
    onCekMulut: () -> Unit,
    tabAwal: Int = 0,
) {
    val context = LocalContext.current
    val vm: EducationViewModel = viewModel(factory = EducationViewModel.factory(context, ambilLabelTerakhir))
    val state by vm.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    EducationScreen(
        state = state,
        tabAwal = tabAwal,
        onBack = onBack,
        onCobaLagi = vm::muat,
        onCekMulut = onCekMulut,
        onBukaKonten = { uriHandler.openUri(it.url) }, // artikel dibuka di browser, video di aplikasi YouTube
    )
}

/** Layar tanpa logika (stateless), supaya mudah dicek lewat Preview untuk setiap kondisi. */
@Composable
fun EducationScreen(
    state: EducationUiState,
    tabAwal: Int,
    onBack: () -> Unit,
    onCobaLagi: () -> Unit,
    onCekMulut: () -> Unit,
    onBukaKonten: (EducationContent) -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(tabAwal) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader(title = "Edukasi Untukmu", onBack = onBack)
        when (state) {
            EducationUiState.Loading -> KondisiTengah {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text("Mencari konten yang paling relevan…", style = MaterialTheme.typography.bodyLarge)
            }

            is EducationUiState.Gagal -> KondisiTengah {
                Text("Gagal memuat konten", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Text(state.pesan, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                GradientButton("Coba Lagi", onClick = onCobaLagi, modifier = Modifier.padding(top = 8.dp))
            }

            is EducationUiState.Sukses -> {
                if (state.personal) {
                    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LabelChip("Berdasarkan hasil: ${state.namaPenyakit ?: "-"}")
                        Text("Diurutkan dari yang paling relevan", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    KartuBelumAdaHasil(onCekMulut)
                }
                PrimaryTabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.background) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Artikel", style = MaterialTheme.typography.titleSmall) })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Video", style = MaterialTheme.typography.titleSmall) })
                }
                val daftar = if (tab == 0) state.artikel else state.video
                if (daftar.isEmpty()) {
                    KondisiTengah { Text("Belum ada konten untuk ditampilkan.", style = MaterialTheme.typography.bodyLarge) }
                } else {
                    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        itemsIndexed(daftar, key = { _, it -> it.konten.url }) { indeks, item ->
                            KartuKonten(item.konten, palingRelevan = state.personal && indeks == 0, onClick = { onBukaKonten(item.konten) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KartuBelumAdaHasil(onCekMulut: () -> Unit) {
    Column(
        Modifier
            .padding(start = 20.dp, end = 20.dp, top = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Belum ada hasil pemeriksaan", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(
            "Lakukan Cek Mulut agar rekomendasi sesuai kondisimu. Sementara itu, berikut konten umum kesehatan mulut.",
            style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
        )
        GradientButton("Cek Mulut Sekarang", onClick = onCekMulut)
    }
}

@Composable
private fun KartuKonten(konten: EducationContent, palingRelevan: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Tempat gambar. Gambar dari internet (urlToImage / thumbnail YouTube) butuh library Coil, bisa menyusul.
        Box(
            Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (konten.tipe == ContentType.VIDEO) "▶" else "Aa", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (palingRelevan) LabelChip("Paling relevan")
            Text(
                konten.judul.orEmpty(), style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary, maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
            val info = listOfNotNull(konten.sumber, konten.publishedAtMillis?.let(::formatTanggal)).joinToString(" · ")
            if (info.isNotEmpty()) Text(info, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun KondisiTengah(isi: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) { isi() }
}

private fun formatTanggal(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID")).format(Date(millis))

// ---------- Preview untuk setiap kondisi (screenshot laporan 4.1 bisa diambil dari sini) ----------

private val contohArtikel = listOf(
    ScoredContent(EducationContent("Gusi Berdarah? Kenali Gejala Awal Gingivitis", null, "u1", ContentType.ARTIKEL, "Contoh Data", null, 1_763_600_000_000), 7),
    ScoredContent(EducationContent("Cara Merawat Gusi agar Tidak Mudah Meradang", null, "u2", ContentType.ARTIKEL, "Contoh Data", null, 1_763_000_000_000), 4),
)

@Preview(name = "6 · Sukses (personal)", showBackground = true, widthDp = 360, heightDp = 700)
@Composable
private fun PreviewSukses() = OralHealthPlusTheme {
    EducationScreen(EducationUiState.Sukses("Radang Gusi (Gingivitis)", contohArtikel, emptyList(), true), 0, {}, {}, {}, {})
}

@Preview(name = "8a · Loading", showBackground = true, widthDp = 360, heightDp = 700)
@Composable
private fun PreviewLoading() = OralHealthPlusTheme { EducationScreen(EducationUiState.Loading, 0, {}, {}, {}, {}) }

@Preview(name = "8b · Belum ada hasil", showBackground = true, widthDp = 360, heightDp = 700)
@Composable
private fun PreviewDefault() = OralHealthPlusTheme {
    EducationScreen(EducationUiState.Sukses(null, contohArtikel, emptyList(), false), 0, {}, {}, {}, {})
}

@Preview(name = "8c · Gagal", showBackground = true, widthDp = 360, heightDp = 700)
@Composable
private fun PreviewGagal() = OralHealthPlusTheme {
    EducationScreen(EducationUiState.Gagal("Artikel dan video belum bisa ditampilkan. Periksa koneksi internetmu, lalu coba lagi."), 0, {}, {}, {}, {})
}
