package com.dpa.cuentaatras.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dpa.cuentaatras.MainActivity
import com.dpa.cuentaatras.R
import com.dpa.cuentaatras.data.Trip
import com.dpa.cuentaatras.data.TripRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Recibe la alarma diaria y la vuelve a programar. También se apunta a los eventos que borran
 * las alarmas (reinicio, actualización de la app, cambio de hora) para restaurarla.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = TripRepository(context)
                val reminder = repository.reminder.first()
                if (intent.action == ACTION_REMIND && reminder.enabled) {
                    notify(context, repository.trips.first())
                }
                ReminderScheduler.apply(context, reminder)
            } finally {
                pending.finish()
            }
        }
    }

    private fun notify(context: Context, trips: List<Trip>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val lines = upcomingLines(context, trips)
        if (lines.isEmpty()) return // no quedan viajes pendientes

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, context.getString(R.string.reminder_channel), NotificationManager.IMPORTANCE_DEFAULT)
        )
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(lines.first())
            .setStyle(NotificationCompat.InboxStyle().also { style -> lines.forEach(style::addLine) })
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    /** Una línea por viaje que aún no ha llegado, del más próximo al más lejano. */
    private fun upcomingLines(context: Context, trips: List<Trip>): List<String> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        return trips
            .map { it to ChronoUnit.DAYS.between(today, Instant.ofEpochMilli(it.targetMillis).atZone(zone).toLocalDate()) }
            .filter { (_, days) -> days >= 0 }
            .sortedBy { (_, days) -> days }
            .map { (trip, days) ->
                when (days) {
                    0L -> context.getString(R.string.reminder_today, trip.title)
                    1L -> context.getString(R.string.reminder_tomorrow, trip.title)
                    else -> context.getString(R.string.reminder_days, days, trip.title)
                }
            }
    }

    companion object {
        const val ACTION_REMIND = "com.dpa.cuentaatras.action.REMIND"
        private const val CHANNEL_ID = "daily_reminder"
        private const val NOTIFICATION_ID = 1
    }
}
