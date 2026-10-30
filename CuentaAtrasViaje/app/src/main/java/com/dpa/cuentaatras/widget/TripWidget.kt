package com.dpa.cuentaatras.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider
import com.dpa.cuentaatras.MainActivity
import com.dpa.cuentaatras.R
import com.dpa.cuentaatras.data.Trip
import com.dpa.cuentaatras.data.TripRepository
import com.dpa.cuentaatras.remainingUntil

/**
 * Widget de pantalla de inicio con los días que faltan para un viaje.
 * Android lo refresca cada 30 min (updatePeriodMillis) y la app lo fuerza al guardar o borrar viajes,
 * así que no muestra segundos: no se pueden actualizar con esa frecuencia.
 */
class TripWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    // Recompone con el tamaño real del widget, que en Android 11 o anterior suele ser el mínimo (110 dp).
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = TripRepository(context)
        provideContent {
            val trips by repository.trips.collectAsState(initial = null)
            val chosenId = currentState<Preferences>()[TRIP_ID]
            val list = trips ?: return@provideContent
            // Si el viaje elegido se borró, se muestra el más próximo.
            val trip = list.firstOrNull { it.id == chosenId } ?: list.first()
            WidgetContent(trip)
        }
    }

    companion object {
        val TRIP_ID = stringPreferencesKey("trip_id")
        private val OPEN_TRIP = ActionParameters.Key<String>(MainActivity.EXTRA_TRIP_ID)
        private const val BASE_SIZE_DP = 110f
        private const val MIN_HEIGHT_FOR_TITLE_DP = 90f

        @Composable
        private fun WidgetContent(trip: Trip) {
            val context = LocalContext.current
            val background = remember(trip.backgroundPath) { trip.backgroundPath?.let(::decodeSampled) }
            val remaining = remainingUntil(trip.targetMillis, System.currentTimeMillis())
            val textColor = Color(trip.textColor)
            val color = ColorProvider(day = textColor, night = textColor) // mismo color: lo elige el usuario
            // Los tamaños están pensados para 110 × 110 dp y crecen con el widget. Lo que no cabe en un
            // widget se recorta sin avisar, así que en tamaños muy bajos se prescinde del título.
            val size = LocalSize.current
            val scale = (minOf(size.width.value, size.height.value) / BASE_SIZE_DP).coerceIn(0.8f, 1.8f)
            val showTitle = size.height.value >= MIN_HEIGHT_FOR_TITLE_DP

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(16.dp)
                    .clickable(actionStartActivity<MainActivity>(actionParametersOf(OPEN_TRIP to trip.id))),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    provider = if (background != null) ImageProvider(background) else ImageProvider(R.drawable.widget_gradient),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = GlanceModifier.fillMaxSize(),
                )
                Box(GlanceModifier.fillMaxSize().background(Color.Black.copy(alpha = trip.dim))) {}
                Column(
                    modifier = GlanceModifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (showTitle) {
                        Text(
                            text = trip.title,
                            maxLines = 1,
                            style = TextStyle(color = color, fontSize = (13 * scale).sp, textAlign = TextAlign.Center),
                        )
                    }
                    if (remaining.isOver) {
                        Text(
                            text = context.getString(R.string.widget_arrived),
                            style = TextStyle(color = color, fontSize = (20 * scale).sp, fontWeight = FontWeight.Bold),
                        )
                    } else {
                        Text(
                            text = remaining.days.toString(),
                            maxLines = 1,
                            style = TextStyle(color = color, fontSize = (34 * scale).sp, fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = context.getString(R.string.widget_days_hours, remaining.hours),
                            maxLines = 1,
                            style = TextStyle(color = color, fontSize = (12 * scale).sp),
                        )
                    }
                }
            }
        }

        /** Los widgets van por RemoteViews, con límite de memoria: se reduce la foto a ~500 px. */
        private fun decodeSampled(path: String, maxSize: Int = 500): Bitmap? {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= maxSize && bounds.outHeight / (sample * 2) >= maxSize) sample *= 2
            return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
        }
    }
}

class TripWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TripWidget()
}
