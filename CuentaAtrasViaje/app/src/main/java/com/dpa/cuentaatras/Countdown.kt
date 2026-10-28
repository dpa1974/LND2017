package com.dpa.cuentaatras

/** Tiempo restante desglosado. Si el momento ya pasó, todo vale 0 e [isOver] es true. */
data class Remaining(
    val days: Long,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
) {
    val isOver: Boolean get() = days == 0L && hours == 0 && minutes == 0 && seconds == 0
}

fun remainingUntil(targetMillis: Long, nowMillis: Long): Remaining {
    val totalSeconds = ((targetMillis - nowMillis) / 1000).coerceAtLeast(0)
    return Remaining(
        days = totalSeconds / 86_400,
        hours = ((totalSeconds % 86_400) / 3_600).toInt(),
        minutes = ((totalSeconds % 3_600) / 60).toInt(),
        seconds = (totalSeconds % 60).toInt(),
    )
}
