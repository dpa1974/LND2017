package com.dpa.cuentaatras.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dpa.cuentaatras.R
import com.dpa.cuentaatras.data.TripSettings
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

private const val MAX_TITLE_LENGTH = 60

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSheet(
    draft: TripSettings,
    onChange: (TripSettings) -> Unit,
    onPickImage: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        scrimColor = Color.Transparent, // deja ver los cambios en vivo por encima del panel
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.edit_title), style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = draft.title,
                onValueChange = { onChange(draft.copy(title = it.take(MAX_TITLE_LENGTH))) },
                label = { Text(stringResource(R.string.field_text)) },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )

            Section(stringResource(R.string.field_date)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(formatDate(draft.targetMillis), maxLines = 1)
                    }
                    OutlinedButton(onClick = { showTimePicker = true }) {
                        Icon(Icons.Filled.Schedule, contentDescription = stringResource(R.string.field_time), Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(formatTime(draft.targetMillis))
                    }
                }
            }

            Section(stringResource(R.string.field_text_color)) {
                ColorPicker(selected = draft.textColor, onSelect = { onChange(draft.copy(textColor = it)) })
            }

            Section(stringResource(R.string.field_background)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onPickImage) {
                        Icon(Icons.Filled.Image, contentDescription = null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.pick_image))
                    }
                    if (draft.backgroundPath != null) {
                        TextButton(onClick = { onChange(draft.copy(backgroundPath = null)) }) {
                            Text(stringResource(R.string.remove_image))
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onSave, enabled = draft.title.isNotBlank()) { Text(stringResource(R.string.save)) }
            }
        }
    }

    if (showDatePicker) {
        TripDatePicker(
            targetMillis = draft.targetMillis,
            onConfirm = { onChange(draft.copy(targetMillis = it)); showDatePicker = false },
            onDismiss = { showDatePicker = false },
        )
    }
    if (showTimePicker) {
        TripTimePicker(
            targetMillis = draft.targetMillis,
            onConfirm = { onChange(draft.copy(targetMillis = it)); showTimePicker = false },
            onDismiss = { showTimePicker = false },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        content()
    }
}

/** Cambia el día manteniendo la hora elegida. El DatePicker trabaja en medianoche UTC. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripDatePicker(targetMillis: Long, onConfirm: (Long) -> Unit, onDismiss: () -> Unit) {
    val zone = ZoneId.systemDefault()
    val current = Instant.ofEpochMilli(targetMillis).atZone(zone)
    val todayUtc = LocalDate.now(zone).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = current.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayUtc
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val picked = state.selectedDateMillis
                if (picked == null) {
                    onDismiss()
                } else {
                    val day = Instant.ofEpochMilli(picked).atZone(ZoneOffset.UTC).toLocalDate()
                    onConfirm(day.atTime(current.toLocalTime()).atZone(zone).toInstant().toEpochMilli())
                }
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripTimePicker(targetMillis: Long, onConfirm: (Long) -> Unit, onDismiss: () -> Unit) {
    val zone = ZoneId.systemDefault()
    val current = Instant.ofEpochMilli(targetMillis).atZone(zone)
    val state = rememberTimePickerState(initialHour = current.hour, initialMinute = current.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val picked = current.toLocalDate().atTime(state.hour, state.minute).atZone(zone)
                onConfirm(picked.toInstant().toEpochMilli())
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        text = { TimePicker(state = state) },
    )
}
