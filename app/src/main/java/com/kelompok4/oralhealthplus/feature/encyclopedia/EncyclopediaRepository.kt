package com.kelompok4.oralhealthplus.feature.encyclopedia

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParseException

/**
 * Sumber data ensiklopedia, dibaca dari assets/encyclopedia.json.
 * Selain untuk layar Ensiklopedia, kelas ini juga menjadi sumber KATA KUNCI untuk fungsi 4.
 */
class EncyclopediaRepository(entries: List<EncyclopediaEntry>) {

    private val byId: Map<String, EncyclopediaEntry> = entries.associateBy { it.id }

    fun semua(): List<EncyclopediaEntry> = byId.values.toList()

    fun cari(id: String?): EncyclopediaEntry? = id?.let { byId[it] }

    /** Kata kunci untuk label hasil deteksi. Kosong jika label tidak dikenal atau null. */
    fun kataKunciUntuk(label: String?): List<String> = cari(label)?.kataKunci.orEmpty()

    companion object {
        const val NAMA_FILE = "encyclopedia.json"

        fun dariAssets(context: Context): EncyclopediaRepository =
            dariJson(context.assets.open(NAMA_FILE).bufferedReader().use { it.readText() })

        /**
         * Membaca teks JSON. Gson bisa mengisi null pada field yang hilang walaupun tipenya non-null,
         * jadi JSON dibaca ke kelas "mentah" (semua nullable) lalu divalidasi satu per satu.
         * Entri yang tidak lengkap dibuang, bukan membuat aplikasi crash.
         */
        fun dariJson(json: String): EncyclopediaRepository {
            val mentah = try {
                Gson().fromJson(json, RawFile::class.java)
            } catch (e: JsonParseException) {
                null
            }
            val valid = mentah?.penyakit.orEmpty().mapNotNull { it?.keEntry() }
            return EncyclopediaRepository(valid)
        }
    }

    private class RawFile(val penyakit: List<RawEntry?>?)

    private class RawEntry(
        val id: String?,
        val nama: String?,
        val ringkasan: String?,
        val gejala: List<String?>?,
        val pencegahan: List<String?>?,
        val kataKunci: List<String?>?,
        val sumber: String?,
    ) {
        fun keEntry(): EncyclopediaEntry? {
            if (id.isNullOrBlank() || nama.isNullOrBlank()) return null
            val kunci = kataKunci.orEmpty().filterNotNull().filter { it.isNotBlank() }
            if (kunci.isEmpty()) return null // tanpa kata kunci, fungsi 4 tidak bisa bekerja
            return EncyclopediaEntry(
                id = id,
                nama = nama,
                ringkasan = ringkasan.orEmpty(),
                gejala = gejala.orEmpty().filterNotNull(),
                pencegahan = pencegahan.orEmpty().filterNotNull(),
                kataKunci = kunci,
                sumber = sumber.orEmpty(),
            )
        }
    }
}
