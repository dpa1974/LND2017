package com.dpa.cuentaatras.data

/** Recordatorio diario de los viajes pendientes. [minuteOfDay] es la hora local en minutos (0..1439). */
data class Reminder(
    val enabled: Boolean,
    val minuteOfDay: Int,
) {
    val hour get() = minuteOfDay / 60
    val minute get() = minuteOfDay % 60

    companion object {
        const val DEFAULT_MINUTE_OF_DAY = 9 * 60
    }
}
