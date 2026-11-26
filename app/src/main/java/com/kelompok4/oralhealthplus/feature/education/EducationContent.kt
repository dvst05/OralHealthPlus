package com.kelompok4.oralhealthplus.feature.education

/** Jenis konten edukasi. Video dari daftar YouTube tidak punya tanggal terbit. */
enum class ContentType { ARTIKEL, VIDEO }

/**
 * Satu konten edukasi (artikel NewsAPI atau video YouTube) sebelum diberi skor.
 * [publishedAtMillis] null berarti tanggal tidak diketahui (misalnya video), sehingga bonus kebaruan = 0.
 * Tanggal disimpan dalam epoch milidetik supaya tidak butuh java.time (minSdk 24).
 */
data class EducationContent(
    val judul: String?,
    val deskripsi: String?,
    val url: String,
    val tipe: ContentType,
    val sumber: String? = null,
    val gambarUrl: String? = null,
    val publishedAtMillis: Long? = null,
)

/** Konten yang sudah dinilai. [skor] dipakai untuk pengurutan. */
data class ScoredContent(
    val konten: EducationContent,
    val skor: Int,
)
