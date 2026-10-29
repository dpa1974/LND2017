package com.dpa.cuentaatras.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.dpa.cuentaatras.R
import com.dpa.cuentaatras.data.Reminder
import com.dpa.cuentaatras.reminder.BackgroundAccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDialog(current: Reminder, onSave: (Reminder) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(current.enabled) }
    var permissionDenied by remember { mutableStateOf(false) }
    val time = rememberTimePickerState(initialHour = current.hour, initialMinute = current.minute, is24Hour = true)

    // Android 13+ pide permiso para mostrar notificaciones; sin él no tiene sentido activar el recordatorio.
    val requestPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        enabled = granted
        permissionDenied = !granted
    }
    fun onToggle(checked: Boolean) {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (checked && needsPermission) {
            requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            enabled = checked
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reminder_title)) },
        text = {
            // Con la sección de segundo plano puede no caber en pantallas pequeñas.
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.reminder_enable))
                    Spacer(Modifier.weight(1f))
                    Switch(checked = enabled, onCheckedChange = ::onToggle)
                }
                if (enabled) {
                    TimeInput(state = time)
                }
                if (enabled) {
                    BackgroundAccessSection()
                }
                Text(
                    stringResource(if (permissionDenied) R.string.reminder_permission_denied else R.string.reminder_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (permissionDenied) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(Reminder(enabled, time.hour * 60 + time.minute)) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

/** Pasos para que Android y la capa del fabricante no frenen el aviso. Se revisa al volver de Ajustes. */
@Composable
private fun BackgroundAccessSection() {
    val context = LocalContext.current
    var unrestricted by remember { mutableStateOf(BackgroundAccess.isUnrestricted(context)) }
    LifecycleResumeEffect(Unit) {
        unrestricted = BackgroundAccess.isUnrestricted(context)
        onPauseOrDispose { }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.background_title), style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(if (unrestricted) R.string.background_battery_ok else R.string.background_battery),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            if (unrestricted) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            } else {
                FilledTonalButton(onClick = { BackgroundAccess.requestUnrestricted(context) }) {
                    Text(stringResource(R.string.background_allow))
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.background_autostart),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            FilledTonalButton(onClick = { BackgroundAccess.openAutostartSettings(context) }) {
                Text(stringResource(R.string.background_open))
            }
        }
        if (BackgroundAccess.hasVendorAutostart) {
            Text(
                stringResource(R.string.background_vendor_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
