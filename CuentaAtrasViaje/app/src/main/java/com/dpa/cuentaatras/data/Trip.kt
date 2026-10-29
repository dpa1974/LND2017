package com.dpa.cuentaatras.data

/**
 * Un viaje con su personalización. [textColor] es ARGB; [backgroundPath] apunta a filesDir;
 * [dim] es la opacidad (0..1) del velo negro sobre el fondo; [font] se aplica al título y los números.
 */
data class Trip(
    val id: String,
    val title: String,
    val targetMillis: Long,
    val textColor: Int,
    val backgroundPath: String?,
    val dim: Float,
    val font: TripFont = TripFont.DEFAULT,
) {
    companion object {
        const val DEFAULT_TEXT_COLOR = 0xFFFFFFFF.toInt()
        const val DEFAULT_DIM = 0.25f
        const val MAX_DIM = 0.8f
    }
}
