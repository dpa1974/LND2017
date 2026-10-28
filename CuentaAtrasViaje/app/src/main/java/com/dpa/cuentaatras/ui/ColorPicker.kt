package com.dpa.cuentaatras.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dpa.cuentaatras.R
import android.graphics.Color as AndroidColor

private val presets = listOf(
    Color.White, Color.Black, Color(0xFFFFD54F), Color(0xFFFF8A65), Color(0xFFE91E63),
    Color(0xFFAB47BC), Color(0xFF42A5F5), Color(0xFF26C6DA), Color(0xFF66BB6A), Color(0xFFF5F5DC),
).map { it.toArgb() }

/** Colores rápidos + selector HSV para cualquier otro. [selected] y el callback usan ARGB. */
@Composable
fun ColorPicker(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    // El HSV se guarda aparte: con saturación 0 el tono se perdería al recalcularlo desde ARGB.
    var hsv by remember { mutableStateOf(selected.toHsv()) }
    LaunchedEffect(selected) {
        if (hsv.toArgb() != selected) hsv = selected.toHsv()
    }
    fun update(newHsv: FloatArray) {
        hsv = newHsv
        onSelect(newHsv.toArgb())
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(presets) { color ->
                Swatch(color, isSelected = color == selected, onClick = { onSelect(color) })
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.custom_color), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(12.dp))
            Box(Modifier.size(24.dp).clip(CircleShape).background(Color(selected)).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape))
        }
        HsvSlider(R.string.hue, hsv[0], 0f..360f) { update(floatArrayOf(it, hsv[1], hsv[2])) }
        HsvSlider(R.string.saturation, hsv[1], 0f..1f) { update(floatArrayOf(hsv[0], it, hsv[2])) }
        HsvSlider(R.string.brightness, hsv[2], 0f..1f) { update(floatArrayOf(hsv[0], hsv[1], it)) }
    }
}

@Composable
private fun Swatch(color: Int, isSelected: Boolean, onClick: () -> Unit) {
    val border = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(color))
            .border(if (isSelected) 3.dp else 1.dp, border, CircleShape)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun HsvSlider(label: Int, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(label), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(80.dp))
        Slider(value = value, onValueChange = onChange, valueRange = range, modifier = Modifier.fillMaxWidth())
    }
}

private fun Int.toHsv() = FloatArray(3).also { AndroidColor.colorToHSV(this, it) }

private fun FloatArray.toArgb() = AndroidColor.HSVToColor(this)
