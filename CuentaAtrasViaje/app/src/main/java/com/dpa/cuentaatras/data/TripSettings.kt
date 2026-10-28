package com.dpa.cuentaatras.data

/** Todo lo que el usuario puede personalizar. [textColor] es ARGB; [backgroundPath] apunta a filesDir. */
data class TripSettings(
    val title: String,
    val targetMillis: Long,
    val textColor: Int,
    val backgroundPath: String?,
)
