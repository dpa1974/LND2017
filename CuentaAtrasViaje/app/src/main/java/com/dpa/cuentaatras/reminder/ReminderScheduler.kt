package com.dpa.cuentaatras.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.dpa.cuentaatras.data.Reminder
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Programa una única alarma para la próxima vez que toca avisar; [ReminderReceiver] programa la siguiente.
 * Se usa una alarma inexacta que sí salta con el móvil bloqueado (setAndAllowWhileIdle): las exactas
 * necesitan un permiso especial que Android 14 deniega por defecto, y para un aviso diario unos minutos
 * de margen no importan.
 */
object ReminderScheduler {

    fun apply(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = pendingIntent(context)
        alarmManager.cancel(intent)
        if (reminder.enabled) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTrigger(reminder), intent)
        }
    }

    internal fun nextTrigger(reminder: Reminder, now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())): Long {
        val today = LocalDate.from(now).atTime(LocalTime.of(reminder.hour, reminder.minute)).atZone(now.zone)
        val next = if (today.isAfter(now)) today else today.plusDays(1)
        return next.toInstant().toEpochMilli()
    }

    private fun pendingIntent(context: Context) = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, ReminderReceiver::class.java).setAction(ReminderReceiver.ACTION_REMIND),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
