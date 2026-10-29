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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dpa.cuentaatras.R
import com.dpa.cuentaatras.data.Trip
import com.dpa.cuentaatras.remainingUntil
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun CountdownScreen(viewModel: CountdownViewModel) {
    val trips by viewModel.trips.collectAsStateWithLifecycle()
    val reminder by viewModel.reminder.collectAsStateWithLifecycle()
    val draft = viewModel.draft
    val context = LocalContext.current
    var showReminder by remember { mutableStateOf(false) }

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
        val list = trips ?: return@Box // DataStore aún cargando: solo unos milisegundos
        val pagerState = rememberPagerState { list.size }

        LaunchedEffect(viewModel.pageToShow, list) {
            val index = list.indexOfFirst { it.id == viewModel.pageToShow }
            if (index >= 0) {
                pagerState.scrollToPage(index)
                viewModel.pageShown()
            }
        }

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = draft == null,
            key = { list[it].id },
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val trip = list[page]
            CountdownPage(if (draft?.id == trip.id) draft else trip)
        }

        if (draft != null && viewModel.isNewDraft) {
            CountdownPage(draft)
        }

        if (draft == null) {
            Row(Modifier.align(Alignment.TopEnd).safeDrawingPadding().padding(8.dp)) {
                IconButton(onClick = { showReminder = true }) {
                    Icon(Icons.Filled.Notifications, contentDescription = stringResource(R.string.reminder_title), tint = Color.White)
                }
                IconButton(onClick = viewModel::startNew) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_trip), tint = Color.White)
                }
            }
            if (list.size > 1) {
                PageIndicator(
                    count = list.size,
                    current = pagerState.currentPage,
                    modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding().padding(bottom = 36.dp),
                )
            }
            FloatingActionButton(
                onClick = { viewModel.startEditing(list[pagerState.currentPage.coerceIn(list.indices)]) },
                modifier = Modifier.align(Alignment.BottomEnd).safeDrawingPadding().padding(16.dp),
            ) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit))
            }
        }
    }

    val currentReminder = reminder
    if (showReminder && currentReminder != null) {
        ReminderDialog(
            current = currentReminder,
            onSave = { viewModel.saveReminder(it); showReminder = false },
            onDismiss = { showReminder = false },
        )
    }

    if (draft != null) {
        EditSheet(
            draft = draft,
            isNew = viewModel.isNewDraft,
            onChange = viewModel::updateDraft,
            onPickImage = {
                pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onSave = viewModel::save,
            onDelete = viewModel::delete,
            onCancel = viewModel::cancel,
        )
    }
}

@Composable
private fun CountdownPage(trip: Trip) {
    Box(Modifier.fillMaxSize()) {
        Background(trip.backgroundPath, trip.dim)
        CountdownContent(trip, Modifier.align(Alignment.Center))
    }
}

@Composable
private fun Background(path: String?, dim: Float) {
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
    // Velo para que el texto se lea sobre cualquier foto; su intensidad la elige el usuario.
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = dim)))
}

@Composable
private fun CountdownContent(trip: Trip, modifier: Modifier = Modifier) {
    val now by rememberNow()
    val remaining = remainingUntil(trip.targetMillis, now)
    val color = Color(trip.textColor)
    val shadow = Shadow(color = Color.Black.copy(alpha = 0.6f), offset = Offset(0f, 2f), blurRadius = 8f)
    val family = trip.font.fontFamily
    // Las fuentes incluidas solo tienen un grosor; forzar negrita las deformaría (negrita sintética).
    val titleStyle = MaterialTheme.typography.headlineLarge.copy(
        fontFamily = family,
        fontWeight = if (family == null) FontWeight.SemiBold else FontWeight.Normal,
        shadow = shadow,
    )
    val numberStyle = MaterialTheme.typography.displayMedium.copy(
        fontFamily = family,
        fontWeight = if (family == null) FontWeight.Bold else FontWeight.Normal,
        shadow = shadow,
    )

    Column(
        modifier = modifier.fillMaxWidth().safeDrawingPadding().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = trip.title,
            color = color,
            textAlign = TextAlign.Center,
            style = titleStyle,
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
                TimeUnit(remaining.days.toString(), stringResource(R.string.unit_days), color, shadow, numberStyle)
                TimeUnit(remaining.hours.pad(), stringResource(R.string.unit_hours), color, shadow, numberStyle)
                TimeUnit(remaining.minutes.pad(), stringResource(R.string.unit_minutes), color, shadow, numberStyle)
                TimeUnit(remaining.seconds.pad(), stringResource(R.string.unit_seconds), color, shadow, numberStyle)
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = formatTarget(trip.targetMillis),
            color = color.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium.copy(shadow = shadow),
        )
    }
}

@Composable
private fun TimeUnit(value: String, label: String, color: Color, shadow: Shadow, numberStyle: TextStyle) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            maxLines = 1,
            style = numberStyle,
        )
        Text(text = label, color = color, style = MaterialTheme.typography.labelLarge.copy(shadow = shadow))
    }
}

@Composable
private fun PageIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(count) { i ->
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (i == current) 1f else 0.4f))
            )
        }
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
