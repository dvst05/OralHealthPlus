package com.kelompok4.oralhealthplus.feature.clinic

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private val ClinicPrimaryBlue = Color(0xFF4A628A)
private val ClinicBackgroundLight = Color(0xFFF8FAFC)
private val ClinicBorderHairline = Color(0xFFE2E8F0)
private val ClinicTextPrimary = Color(0xFF0F172A)
private val ClinicTextMuted = Color(0xFF475569)
private val ClinicStarAmber = Color(0xFFD97706)

@Composable
fun ClinicRoute(
    vm: ClinicViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        vm.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            if (uiState.clinics.isEmpty() && !uiState.isLoading) {
                vm.onPermissionResult(true)
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    ClinicScreen(
        uiState = uiState,
        onRefresh = {
            if (uiState.hasLocationPermission) {
                vm.loadNearbyClinics(forceRefresh = true)
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        },
        onSelectRadius = { vm.updateRadius(it) },
        onClearError = { vm.clearError() },
        onOpenMaps = { clinic -> openGoogleMapsDirection(context, clinic) },
        onCallClinic = { phone -> dialPhoneNumber(context, phone) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicScreen(
    uiState: ClinicUiState,
    onRefresh: () -> Unit,
    onSelectRadius: (Int) -> Unit,
    onClearError: () -> Unit,
    onOpenMaps: (DentalClinic) -> Unit,
    onCallClinic: (String) -> Unit
) {
    val radiusOptions = listOf(5, 10, 15)

    Scaffold(
        containerColor = ClinicBackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Rekomendasi Klinik Terdekat",
                            color = ClinicTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Urutan berdasarkan jarak, rating & jam operasional",
                            color = ClinicTextMuted,
                            fontSize = 12.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !uiState.isLoading) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Muat ulang lokasi",
                            tint = ClinicPrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GpsStatusBanner(
                hasPermission = uiState.hasLocationPermission,
                clinicCount = uiState.clinics.size,
                radiusKm = uiState.selectedRadiusKm,
                onRequestPermission = onRefresh
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(radiusOptions) { km ->
                    val selected = uiState.selectedRadiusKm == km
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectRadius(km) },
                        label = {
                            Text(
                                text = "Radius $km km",
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White,
                            labelColor = ClinicTextPrimary,
                            selectedContainerColor = ClinicPrimaryBlue.copy(alpha = 0.12f),
                            selectedLabelColor = ClinicPrimaryBlue
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (selected) ClinicPrimaryBlue else ClinicBorderHairline
                        )
                    )
                }
            }

            if (uiState.userLat != null && uiState.userLon != null) {
                ClinicMapView(
                    userLat = uiState.userLat,
                    userLon = uiState.userLon,
                    clinics = uiState.clinics
                )
            }

            if (uiState.errorMessage != null) {
                ClinicErrorBanner(message = uiState.errorMessage, onDismiss = onClearError)
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = ClinicPrimaryBlue)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Mencari rekomendasi klinik terbaik...",
                                color = ClinicTextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                uiState.clinics.isEmpty() -> {
                    EmptyClinicCard(
                        hasPermission = uiState.hasLocationPermission,
                        radiusKm = uiState.selectedRadiusKm,
                        onAction = onRefresh
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(uiState.clinics, key = { _, clinic -> clinic.id }) { index, clinic ->
                            DentalClinicCard(
                                rank = index + 1,
                                clinic = clinic,
                                onOpenMaps = { onOpenMaps(clinic) },
                                onCallClinic = { clinic.phone?.let(onCallClinic) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClinicMapView(
    userLat: Double,
    userLon: Double,
    clinics: List<DentalClinic>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Configuration.getInstance().userAgentValue = context.packageName

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(195.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ClinicBorderHairline)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp)),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(14.5)
                    controller.setCenter(GeoPoint(userLat, userLon))
                }
            },
            update = { mapView ->
                mapView.overlays.clear()

                val userMarker = Marker(mapView).apply {
                    position = GeoPoint(userLat, userLon)
                    title = "Lokasi Anda"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(userMarker)

                clinics.forEachIndexed { idx, clinic ->
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(clinic.latitude, clinic.longitude)
                        title = "#${idx + 1} ${clinic.name}"
                        snippet = "${clinic.formattedDistance} • Rating ${clinic.formattedRating}"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mapView.overlays.add(marker)
                }

                mapView.controller.animateTo(GeoPoint(userLat, userLon))
                mapView.invalidate()
            }
        )
    }
}

@Composable
private fun GpsStatusBanner(
    hasPermission: Boolean,
    clinicCount: Int,
    radiusKm: Int,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ClinicBorderHairline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = null,
                tint = ClinicPrimaryBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasPermission) "Lokasi Pengguna Terdeteksi" else "Izin Lokasi Belum Aktif",
                    color = ClinicTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (hasPermission) {
                        "$clinicCount klinik diurutkan berdasarkan skor rekomendasi (radius $radiusKm km)"
                    } else {
                        "Aktifkan izin lokasi untuk menghitung rekomendasi klinik terdekat"
                    },
                    color = ClinicTextMuted,
                    fontSize = 12.sp
                )
            }
            if (!hasPermission) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicPrimaryBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Izinkan", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun DentalClinicCard(
    rank: Int,
    clinic: DentalClinic,
    onOpenMaps: () -> Unit,
    onCallClinic: () -> Unit
) {
    val hasSchedule = !clinic.openingHours.isNullOrBlank() &&
            !clinic.openingHours.equals("Jadwal tidak tercantum di peta", ignoreCase = true)

    val statusBg = when {
        !hasSchedule -> Color(0xFFF1F5F9)
        clinic.isOpen -> Color(0xFFE8F5E9)
        else -> Color(0xFFFFEBEE)
    }
    val statusFg = when {
        !hasSchedule -> Color(0xFF475569)
        clinic.isOpen -> Color(0xFF1E6B34)
        else -> Color(0xFF9B1C1C)
    }
    val statusText = when {
        !hasSchedule -> "Jadwal Tidak Tercantum"
        clinic.isOpen -> "Buka"
        else -> "Tutup"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = if (rank == 1) 1.5.dp else 1.dp,
            color = if (rank == 1) ClinicPrimaryBlue else ClinicBorderHairline
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Baris atas: Badge Peringkat + Skor Rekomendasi & Status Buka/Tutup
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(ClinicPrimaryBlue, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (rank == 1) "Rekomendasi #1" else "#$rank",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(ClinicPrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusFg,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Gambar Placeholder + Nama Klinik + Rating & Jumlah Ulasan
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(ClinicPrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalHospital,
                        contentDescription = "Ikon Klinik",
                        tint = ClinicPrimaryBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = clinic.name,
                        color = ClinicTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = ClinicStarAmber,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${clinic.formattedRating} (${clinic.reviewCount} ulasan)",
                            color = ClinicTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "  •  ",
                            color = ClinicTextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = clinic.formattedDistance,
                            color = ClinicPrimaryBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Alamat Klinik (tanpa menampilkan koordinat mentah)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = ClinicTextMuted,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = clinic.address,
                    color = ClinicTextMuted,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!clinic.openingHours.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = ClinicTextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = clinic.openingHours,
                        color = ClinicTextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tombol Aksi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenMaps,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicPrimaryBlue)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Directions,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buka Rute Maps", fontSize = 13.sp, color = Color.White)
                }

                if (!clinic.phone.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = onCallClinic,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ClinicBorderHairline)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Telepon klinik",
                            tint = ClinicPrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyClinicCard(
    hasPermission: Boolean,
    radiusKm: Int,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ClinicBorderHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.LocalHospital,
                contentDescription = null,
                tint = ClinicPrimaryBlue,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (hasPermission) {
                    "Belum ada klinik gigi dalam radius $radiusKm km"
                } else {
                    "Akses Lokasi Diperlukan"
                },
                color = ClinicTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (hasPermission) {
                    "Coba perbesar pilihan radius pencarian di atas atau tekan muat ulang."
                } else {
                    "Izinkan akses lokasi agar OralHealth+ dapat merekomendasikan klinik gigi terdekat."
                },
                color = ClinicTextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ClinicPrimaryBlue)
            ) {
                Text(
                    text = if (hasPermission) "Cari Ulang" else "Izinkan Lokasi",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ClinicErrorBanner(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = Color(0xFF9B1C1C),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                color = Color(0xFF9B1C1C),
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Tutup galat",
                    tint = Color(0xFF9B1C1C),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun openGoogleMapsDirection(context: Context, clinic: DentalClinic) {
    // Gabungkan nama klinik dan alamat (jika valid) agar Google Maps langsung membuka Place Page-nya
    val cleanAddress = if (
        clinic.address.isNotBlank() &&
        !clinic.address.startsWith("Area Layanan") &&
        !clinic.address.startsWith("Alamat tidak")
    ) {
        "${clinic.name}, ${clinic.address}"
    } else {
        "${clinic.name}, Malang"
    }

    val encodedQuery = Uri.encode(cleanAddress)
    // geo:lat,lon?q=Nama+Klinik -> memfokuskan pencarian di sekitar koordinat klinik sekaligus membuka profil tempatnya
    val geoUri = Uri.parse("geo:${clinic.latitude},${clinic.longitude}?q=$encodedQuery")

    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        setPackage("com.google.android.apps.maps")
    }

    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        val browserUri = Uri.parse(
            "https://www.google.com/maps/search/?api=1&query=$encodedQuery"
        )
        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
    }
}

private fun dialPhoneNumber(context: Context, phone: String) {
    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
    context.startActivity(dialIntent)
}