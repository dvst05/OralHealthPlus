package com.kelompok4.oralhealthplus.feature.education

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Sumber artikel. Dibuat sebagai interface supaya bisa diganti data contoh (offline) atau data palsu saat unit test. */
fun interface ArticleSource {
    /** @throws IOException jika data gagal diambil (internet mati, kunci API salah, dan sebagainya). */
    suspend fun cariArtikel(kataKunci: List<String>): List<EducationContent>
}

/** Mengambil artikel dari NewsAPI endpoint /v2/everything. */
class NewsApiArticleSource(
    private val apiKey: String,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build(),
) : ArticleSource {

    override suspend fun cariArtikel(kataKunci: List<String>): List<EducationContent> =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(buatUrl(kataKunci))
                .header("X-Api-Key", apiKey) // kunci dikirim lewat header, tidak tertulis di URL
                .build()
            client.newCall(request).execute().use { respons ->
                val body = respons.body?.string() ?: throw IOException("Respons kosong")
                when (val hasil = NewsApiParser.parse(body)) {
                    is NewsApiParser.Hasil.Sukses -> hasil.artikel
                    is NewsApiParser.Hasil.Gagal -> throw IOException(hasil.pesan)
                }
            }
        }

    companion object {
        private const val BASE_URL = "https://newsapi.org/v2/everything"
        const val MAKS_PANJANG_QUERY = 500 // batas parameter q di NewsAPI

        /**
         * Kata kunci digabung dengan OR. Frasa berisi spasi diberi tanda kutip
         * supaya dicari sebagai satu frasa: gingivitis OR "gusi berdarah".
         */
        fun buatQuery(kataKunci: List<String>): String {
            val bagian = kataKunci.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
                .map { if (it.contains(' ')) "\"$it\"" else it }
            val hasil = StringBuilder()
            for (b in bagian) {
                val tambahan = if (hasil.isEmpty()) b else " OR $b"
                if (hasil.length + tambahan.length > MAKS_PANJANG_QUERY) break
                hasil.append(tambahan)
            }
            return hasil.toString()
        }

        fun buatUrl(kataKunci: List<String>): String =
            BASE_URL.toHttpUrl().newBuilder()
                .addQueryParameter("q", buatQuery(kataKunci))
                .addQueryParameter("searchIn", "title,description")
                .addQueryParameter("sortBy", "publishedAt")
                .addQueryParameter("pageSize", "50")
                .build()
                .toString()
    }
}

/**
 * Sumber artikel offline dari assets/newsapi_sample.json (data contoh, BUKAN berita nyata).
 * Dipakai saat kunci NewsAPI belum diisi atau kuota habis, sesuai rencana cadangan di jobdesk.
 */
class OfflineArticleSource(private val context: Context) : ArticleSource {
    override suspend fun cariArtikel(kataKunci: List<String>): List<EducationContent> =
        withContext(Dispatchers.IO) {
            val json = context.assets.open("newsapi_sample.json").bufferedReader().use { it.readText() }
            when (val hasil = NewsApiParser.parse(json)) {
                is NewsApiParser.Hasil.Sukses -> hasil.artikel
                is NewsApiParser.Hasil.Gagal -> throw IOException(hasil.pesan)
            }
        }
}
