package com.kelompok4.oralhealthplus.feature.encyclopedia

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kelompok4.oralhealthplus.ui.components.ScreenHeader
import com.kelompok4.oralhealthplus.ui.theme.OralHealthPlusTheme

/**
 * Layar daftar penyakit. Navigasi tidak diatur di sini: layar hanya memanggil [onPilih] dan [onBack],
 * nanti NavHost (Anggota 3) yang memutuskan pindah ke mana.
 */
@Composable
fun EncyclopediaListScreen(
    daftar: List<EncyclopediaEntry>,
    onPilih: (EncyclopediaEntry) -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader(title = "Info Penyakit", onBack = onBack)
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(daftar, key = { it.id }) { entri ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        .clickable { onPilih(entri) }
                        .padding(16.dp),
                ) {
                    Text(entri.nama, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Text(entri.ringkasan, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
            }
        }
    }
}

/** Layar detail satu penyakit: ringkasan, gejala, pencegahan, sumber. */
@Composable
fun EncyclopediaDetailScreen(entri: EncyclopediaEntry, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader(title = "Detail Penyakit", onBack = onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(entri.nama, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text(entri.ringkasan, style = MaterialTheme.typography.bodyMedium)
            Bagian("Gejala", entri.gejala)
            Bagian("Pencegahan", entri.pencegahan)
            Text(
                "Sumber: ${entri.sumber}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                "Informasi ini bersifat edukasi dan tidak menggantikan pemeriksaan dokter gigi.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun Bagian(judul: String, isi: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(judul, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        isi.forEach { baris ->
            Row {
                Text("•  ", style = MaterialTheme.typography.bodyMedium)
                Text(baris, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private val contoh = EncyclopediaEntry(
    id = "Gingivitis",
    nama = "Radang Gusi (Gingivitis)",
    ringkasan = "Peradangan pada gusi yang umumnya disebabkan oleh penumpukan plak.",
    gejala = listOf("Gusi merah dan bengkak", "Gusi mudah berdarah"),
    pencegahan = listOf("Sikat gigi dua kali sehari", "Gunakan benang gigi"),
    kataKunci = listOf("gingivitis"),
    sumber = "https://www.nhs.uk/conditions/gum-disease/",
)

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ListPreview() {
    OralHealthPlusTheme { EncyclopediaListScreen(listOf(contoh), onPilih = {}, onBack = {}) }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun DetailPreview() {
    OralHealthPlusTheme { EncyclopediaDetailScreen(contoh, onBack = {}) }
}
