package com.dpa.cuentaatras.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.dpa.cuentaatras.R
import com.dpa.cuentaatras.data.Trip
import com.dpa.cuentaatras.data.TripRepository
import com.dpa.cuentaatras.ui.AppTheme
import com.dpa.cuentaatras.ui.formatTarget
import kotlinx.coroutines.launch

/** Se abre al añadir (o reconfigurar) el widget para elegir qué viaje muestra. */
class TripWidgetConfigActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val widgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        // Si el usuario sale sin elegir, Android descarta el widget.
        setResult(RESULT_CANCELED, result)
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val repository = TripRepository(this)
        setContent {
            AppTheme {
                val trips by repository.trips.collectAsState(initial = emptyList())
                Surface(Modifier.fillMaxSize()) {
                    LazyColumn(Modifier.safeDrawingPadding()) {
                        item {
                            ListItem(headlineContent = {
                                Text(stringResource(R.string.widget_pick_trip), style = MaterialTheme.typography.titleLarge)
                            })
                        }
                        items(trips, key = { it.id }) { trip ->
                            ListItem(
                                headlineContent = { Text(trip.title) },
                                supportingContent = { Text(formatTarget(trip.targetMillis)) },
                                modifier = Modifier.clickable { choose(widgetId, trip, result) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    private fun choose(widgetId: Int, trip: Trip, result: Intent) {
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(this@TripWidgetConfigActivity).getGlanceIdBy(widgetId)
            updateAppWidgetState(this@TripWidgetConfigActivity, glanceId) { it[TripWidget.TRIP_ID] = trip.id }
            TripWidget().update(this@TripWidgetConfigActivity, glanceId)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}
