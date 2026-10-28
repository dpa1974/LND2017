package com.dpa.cuentaatras.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun formatDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(dateFormatter)

fun formatTime(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(timeFormatter)

fun formatTarget(millis: Long): String = "${formatDate(millis)} · ${formatTime(millis)}"
