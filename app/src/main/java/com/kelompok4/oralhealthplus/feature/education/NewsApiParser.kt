package com.kelompok4.oralhealthplus.feature.education

import com.google.gson.Gson
import com.google.gson.JsonParseException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Mengubah teks JSON dari NewsAPI (/v2/everything) menjadi daftar [EducationContent].
 *
 * Bentuk respons sukses:
 *   {"status":"ok","totalResults":2,"articles":[{"source":{"name":"..."},"title":"...","description":"...",
 *    "url":"...","urlToImage":"...","publishedAt":"2026-11-20T10:00:00Z"}]}
 * Bentuk respons gagal:
 *   {"status":"error","code":"apiKeyInvalid","message":"..."}
 */
object NewsApiParser {

    sealed class Hasil {
        data class Sukses(val artikel: List<EducationContent>) : Hasil()
        data class Gagal(val pesan: String) : Hasil()
    }

    fun parse(json: String): Hasil {
        val respons = try {
            Gson().fromJson(json, RawResponse::class.java)
        } catch (e: JsonParseException) {
            return Hasil.Gagal("Format respons tidak dikenali")
        } ?: return Hasil.Gagal("Respons kosong")

        if (respons.status != "ok") {
            return Hasil.Gagal(respons.message ?: "NewsAPI mengembalikan error (${respons.code ?: "tanpa kode"})")
        }
        val artikel = respons.articles.orEmpty().mapNotNull { it?.keKonten() }
        return Hasil.Sukses(artikel)
    }

    /** "2026-11-20T10:00:00Z" -> epoch milidetik, atau null jika format tidak sesuai. */
    fun parseTanggal(iso: String?): Long? {
        if (iso.isNullOrBlank()) return null
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return try {
            // Potong bagian detik pecahan dan zona ("Z", ".123Z") karena NewsAPI selalu memakai UTC
            format.parse(iso.take(19))?.time
        } catch (e: ParseException) {
            null
        }
    }

    private class RawResponse(
        val status: String?,
        val code: String?,
        val message: String?,
        val articles: List<RawArticle?>?,
    )

    private class RawSource(val name: String?)

    private class RawArticle(
        val source: RawSource?,
        val title: String?,
        val description: String?,
        val url: String?,
        val urlToImage: String?,
        val publishedAt: String?,
    ) {
        fun keKonten(): EducationContent? {
            // URL wajib ada karena kartu harus bisa dibuka.
            if (url.isNullOrBlank()) return null
            // NewsAPI menandai artikel yang sudah dihapus penerbitnya dengan judul "[Removed]".
            if (title.equals("[Removed]", ignoreCase = true)) return null
            // Judul kosong TIDAK dibuang di sini: itu tugas ContentRecommender, sesuai flowchart.
            return EducationContent(
                judul = title,
                deskripsi = description,
                url = url,
                tipe = ContentType.ARTIKEL,
                sumber = source?.name,
                gambarUrl = urlToImage,
                publishedAtMillis = parseTanggal(publishedAt),
            )
        }
    }
}
