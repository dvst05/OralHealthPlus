package com.kelompok4.oralhealthplus.feature.detection

import com.kelompok4.oralhealthplus.data.model.DetectionResult as HasilBersama
import com.kelompok4.oralhealthplus.data.model.Urgensi as UrgensiBersama

/**
 * Jembatan ke model di data/model yang dipakai HistoryRepository (Anggota 2).
 * Saat ini ada dua `DetectionResult` dengan bentuk sama; fungsi ini menyambungkannya tanpa
 * mengubah kode anggota lain. Nama konstanta Urgensi di kedua enum harus tetap sama
 * (diuji di IntegrasiRiwayatTest).
 */
fun DetectionResult.keModelBersama(): HasilBersama = HasilBersama(
    kelas = kelas,
    confidence = confidence,
    urgensi = urgensi?.let { UrgensiBersama.valueOf(it.name) },
    ditolak = ditolak
)
