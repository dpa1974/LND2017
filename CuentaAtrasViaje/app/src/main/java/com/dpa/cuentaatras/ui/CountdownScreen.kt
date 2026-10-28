package com.dpa.cuentaatras.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dpa.cuentaatras.R
import com.dpa.cuentaatras.data.TripSettings
import com.dpa.cuentaatras.remainingUntil
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun CountdownScreen(viewModel: CountdownViewModel = viewModel()) {
    val saved by viewModel.settings.collectAsStateWithLifecycle()
    val shown = viewModel.draft ?: saved
    val context = LocalContext.current

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.importBackground(uri)
    }

    LaunchedEffect(viewModel.imageError) {
        if (viewModel.imageError) {
            Toast.makeText(context, R.string.image_error, Toast.LENGTH_SHORT).show()
            viewModel.imageErrorShown()
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (shown == null) return@Box // DataStore aún cargando: solo unos milisegundos

        Background(shown.backgroundPath)
        CountdownContent(shown, Modifier.align(Alignment.Center))

        if (viewModel.draft == null) {
            FloatingActionButton(
                onClick = viewModel::startEditing,
                modifier = Modifier.align(Alignment.BottomEnd).safeDrawingPadding().padding(16.dp),
            ) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit))
            }
        }
    }

    viewModel.draft?.let { draft ->
        EditSheet(
            draft = draft,
            onChange = viewModel::updateDraft,
            onPickImage = {
                pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onSave = viewModel::save,
            onCancel = viewModel::cancel,
        )
    }
}

@Composable
private fun Background(path: String?) {
    if (path != null) {
        AsyncImage(
            model = File(path),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF7C3AED), Color(0xFFDB2777)))
            )
        )
    }
    // Velo para que el texto se lea sobre cualquier foto.
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)))
}

@Composable
private fun CountdownContent(settings: TripSettings, modifier: Modifier = Modifier) {
    val now by rememberNow()
    val remaining = remainingUntil(settings.targetMillis, now)
    val color = Color(settings.textColor)
    val shadow = Shadow(color = Color.Black.copy(alpha = 0.6f), offset = Offset(0f, 2f), blurRadius = 8f)

    Column(
        modifier = modifier.fillMaxWidth().safeDrawingPadding().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = settings.title,
            color = color,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold, shadow = shadow),
        )
        Spacer(Modifier.height(32.dp))

        if (remaining.isOver) {
            Text(
                text = stringResource(R.string.arrived),
                color = color,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(shadow = shadow),
            )
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TimeUnit(remaining.days.toString(), stringResource(R.string.unit_days), color, shadow)
                TimeUnit(remaining.hours.pad(), stringResource(R.string.unit_hours), color, shadow)
                TimeUnit(remaining.minutes.pad(), stringResource(R.string.unit_minutes), color, shadow)
                TimeUnit(remaining.seconds.pad(), stringResource(R.string.unit_seconds), color, shadow)
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = formatTarget(settings.targetMillis),
            color = color.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium.copy(shadow = shadow),
        )
    }
}

@Composable
private fun TimeUnit(value: String, label: String, color: Color, shadow: Shadow) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            maxLines = 1,
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold, shadow = shadow),
        )
        Text(text = label, color = color, style = MaterialTheme.typography.labelLarge.copy(shadow = shadow))
    }
}

private fun Int.pad() = toString().padStart(2, '0')

/** Hora actual que se refresca justo al cambiar cada segundo. */
@Composable
private fun rememberNow(): State<Long> = produceState(System.currentTimeMillis()) {
    while (true) {
        value = System.currentTimeMillis()
        delay(1_000 - value % 1_000)
    }
}
