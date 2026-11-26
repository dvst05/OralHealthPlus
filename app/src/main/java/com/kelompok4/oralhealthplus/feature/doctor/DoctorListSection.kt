package com.kelompok4.oralhealthplus.feature.doctor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kelompok4.oralhealthplus.data.local.entity.DetectionHistoryEntity
import java.util.Locale
import com.kelompok4.oralhealthplus.ui.theme.BluePrimary
import com.kelompok4.oralhealthplus.ui.theme.ChipBlue
import com.kelompok4.oralhealthplus.ui.theme.GreenSoft
import com.kelompok4.oralhealthplus.ui.theme.GreenSuccess
import com.kelompok4.oralhealthplus.ui.theme.StarAmber
import com.kelompok4.oralhealthplus.ui.theme.SurfaceBlue

private val DocPrimaryBlue = BluePrimary
private val DocBorderHairline = SurfaceBlue
private val DocTextPrimary = BluePrimary
private val DocTextMuted = BluePrimary
private val DocWhatsAppGreen = GreenSuccess
private val DocWhatsAppSoftBg = GreenSoft
private val DocStarAmber = StarAmber

@Composable
fun DoctorListSection(
    latestDetection: DetectionHistoryEntity?,
    onDoctorClick: (DentalDoctor) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedDoctors = remember(latestDetection?.kelas) {
        DoctorCatalog.getSortedDoctors(latestDetection?.kelas)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DocBorderHairline)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Konsultasi Langsung dengan Dokter Gigi",
                        color = DocTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (latestDetection != null) {
                            "Ketuk kartu dokter untuk langsung terhubung via WhatsApp dengan menyertakan hasil scan terakhir Anda (${latestDetection.kelas.replace("_", " ")})."
                        } else {
                            "Ketuk kartu dokter di bawah ini untuk langsung diarahkan ke WhatsApp konsultasi dokter gigi."
                        },
                        color = DocTextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        items(sortedDoctors, key = { it.id }) { doctor ->
            val isRecommended = DoctorCatalog.isRecommendedFor(doctor, latestDetection?.kelas)
            DoctorCard(
                doctor = doctor,
                isRecommendedForScan = isRecommended,
                onClick = { onDoctorClick(doctor) }
            )
        }
    }
}

@Composable
private fun DoctorCard(
    doctor: DentalDoctor,
    isRecommendedForScan: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = 1.dp,
            color = if (isRecommendedForScan) DocPrimaryBlue.copy(alpha = 0.45f) else DocBorderHairline
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DocPrimaryBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = DocPrimaryBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = doctor.name,
                            color = DocTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = doctor.specialization,
                        color = DocPrimaryBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRecommendedForScan) {
                    Surface(
                        color = DocPrimaryBlue.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Sesuai Hasil Scan",
                            color = DocPrimaryBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Surface(
                    color = if (doctor.isAvailableNow) DocWhatsAppSoftBg else ChipBlue,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (doctor.isAvailableNow) "Tersedia Hari Ini" else "Jadwal Terbatas",
                        color = if (doctor.isAvailableNow) DocWhatsAppGreen else DocTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = DocStarAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f • %d thn", doctor.rating, doctor.experienceYears),
                        color = DocTextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocalHospital,
                    contentDescription = null,
                    tint = DocTextMuted,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = doctor.clinicName,
                    color = DocTextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = DocTextMuted,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = doctor.schedule,
                    color = DocTextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = DocWhatsAppGreen),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hubungi via WhatsApp (+${doctor.whatsappNumber})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
