package com.kelompok4.oralhealthplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kelompok4.oralhealthplus.ui.theme.BlueLight
import com.kelompok4.oralhealthplus.ui.theme.OralHealthPlusTheme
import com.kelompok4.oralhealthplus.ui.theme.PrimaryGradient

/**
 * Header standar setiap layar: tombol kembali, judul di tengah, garis biru muda di bawahnya.
 * Contoh: ScreenHeader(title = "Result", onBack = { navController.popBackStack() })
 */
@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).semantics { contentDescription = "Kembali" }) {
                    // Panah "<" digambar manual supaya tidak butuh library ikon tambahan
                    val arrowColor = MaterialTheme.colorScheme.primary
                    Canvas(Modifier.size(20.dp)) {
                        val w = size.width
                        val h = size.height
                        drawLine(arrowColor, Offset(w * 0.65f, h * 0.1f), Offset(w * 0.3f, h * 0.5f), strokeWidth = 6f, cap = StrokeCap.Round)
                        drawLine(arrowColor, Offset(w * 0.3f, h * 0.5f), Offset(w * 0.65f, h * 0.9f), strokeWidth = 6f, cap = StrokeCap.Round)
                    }
                }
            }
            Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        }
        HorizontalDivider(Modifier.padding(horizontal = 25.dp), thickness = 2.dp, color = BlueLight)
    }
}

/** Tombol bulat penuh berwarna polos. Default biru ("Try Again"); untuk "Finish" isi color = tertiary. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Tombol gradien biru seperti "Scan Now" di Home. */
@Composable
fun GradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(PrimaryGradient)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

/** Chip label kecil: "Article", "Video", "Certainty 87%". */
@Composable
fun LabelChip(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ComponentsPreview() {
    OralHealthPlusTheme {
        Column {
            ScreenHeader(title = "Result", onBack = {})
            Column(Modifier.padding(16.dp)) {
                LabelChip("Certainty 87%")
                Box(Modifier.size(12.dp))
                GradientButton("Scan Now", onClick = {})
                Box(Modifier.size(12.dp))
                PillButton("Finish", onClick = {}, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}
