package com.kelompok4.oralhealthplus.feature.clinic

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

data class DentalClinic(
    val id: String,
    val name: String,
    val address: String,
    val phone: String?,
    val openingHours: String?,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Float
) {
    val formattedDistance: String
        get() = if (distanceMeters < 1000f) {
            "${distanceMeters.toInt()} m"
        } else {
            String.format(Locale.US, "%.1f km", distanceMeters / 1000f)
        }
}

class ClinicRepository(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Tiga server mirror Overpass agar jika satu sibuk otomatis pindah ke cadangan
    private val overpassEndpoints = listOf(
        "https://overpass-api.de/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter",
        "https://maps.mail.ru/osm/tools/overpass/api/interpreter"
    )

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()

        fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(location)
                } else {
                    fusedClient.lastLocation
                        .addOnSuccessListener { lastLoc -> cont.resume(lastLoc) }
                        .addOnFailureListener { cont.resume(null) }
                }
            }
            .addOnFailureListener {
                fusedClient.lastLocation
                    .addOnSuccessListener { lastLoc -> cont.resume(lastLoc) }
                    .addOnFailureListener { cont.resume(null) }
            }

        cont.invokeOnCancellation { cts.cancel() }
    }

    suspend fun fetchNearbyClinics(
        userLat: Double,
        userLon: Double,
        radiusMeters: Int = 5000
    ): Result<List<DentalClinic>> = withContext(Dispatchers.IO) {
        val overpassQuery = """
            [out:json][timeout:12];
            (
              node["amenity"="dentist"](around:$radiusMeters,$userLat,$userLon);
              way["amenity"="dentist"](around:$radiusMeters,$userLat,$userLon);
              node["healthcare"="dentist"](around:$radiusMeters,$userLat,$userLon);
              way["healthcare"="dentist"](around:$radiusMeters,$userLat,$userLon);
            );
            out center tags;
        """.trimIndent()

        val body = ("data=" + URLEncoder.encode(overpassQuery, "UTF-8"))
            .toRequestBody("application/x-www-form-urlencoded".toMediaType())

        var lastError: Exception? = null

        for (endpoint in overpassEndpoints) {
            try {
                val request = Request.Builder()
                    .url(endpoint)
                    .header("User-Agent", "OralHealthPlus-AndroidApp/1.0")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        lastError = Exception("HTTP ${response.code}")
                        return@use
                    }
                    val rawJson = response.body?.string().orEmpty()
                    val parsed = parseClinicsJson(rawJson, userLat, userLon)
                    return@withContext Result.success(parsed)
                }
            } catch (e: Exception) {
                lastError = e
            }
        }

        Result.failure(Exception("Server peta sedang sibuk (${lastError?.message ?: "timeout"}). Coba tekan Refresh."))
    }

    private fun parseClinicsJson(
        rawJson: String,
        userLat: Double,
        userLon: Double
    ): List<DentalClinic> {
        val root = JsonParser.parseString(rawJson).asJsonObject
        val elements = root.getAsJsonArray("elements") ?: return emptyList()

        val clinics = mutableListOf<DentalClinic>()
        for (el in elements) {
            val obj = el.asJsonObject
            val tags = obj.getAsJsonObject("tags") ?: continue
            val name = tags.get("name")?.asString?.takeIf { it.isNotBlank() }
                ?: "Klinik / Praktik Dokter Gigi"

            val lat = obj.get("lat")?.asDouble
                ?: obj.getAsJsonObject("center")?.get("lat")?.asDouble
                ?: continue
            val lon = obj.get("lon")?.asDouble
                ?: obj.getAsJsonObject("center")?.get("lon")?.asDouble
                ?: continue

            val street = tags.get("addr:street")?.asString
            val city = tags.get("addr:city")?.asString
            val fullAddr = tags.get("addr:full")?.asString
            val address = when {
                !fullAddr.isNullOrBlank() -> fullAddr
                !street.isNullOrBlank() && !city.isNullOrBlank() -> "$street, $city"
                !street.isNullOrBlank() -> street
                !city.isNullOrBlank() -> city
                else -> "Koordinat: ${String.format(Locale.US, "%.4f, %.4f", lat, lon)}"
            }

            val phone = tags.get("phone")?.asString ?: tags.get("contact:phone")?.asString
            val hours = tags.get("opening_hours")?.asString

            val distResult = FloatArray(1)
            Location.distanceBetween(userLat, userLon, lat, lon, distResult)

            clinics.add(
                DentalClinic(
                    id = "${obj.get("type")?.asString}_${obj.get("id")?.asLong}",
                    name = name,
                    address = address,
                    phone = phone,
                    openingHours = hours,
                    latitude = lat,
                    longitude = lon,
                    distanceMeters = distResult[0]
                )
            )
        }
        return clinics.sortedBy { it.distanceMeters }
    }
}