package com.dpa.cuentaatras.ui

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.dpa.cuentaatras.R
import com.dpa.cuentaatras.data.TripFont

/** Fuentes incluidas en res/font (Google Fonts, licencias en licenses/fonts). null = la del tema. */
val TripFont.fontFamily: FontFamily?
    get() = when (this) {
        TripFont.DEFAULT -> null
        TripFont.PACIFICO -> FontFamily(Font(R.font.pacifico))
        TripFont.LOBSTER -> FontFamily(Font(R.font.lobster))
        TripFont.BEBAS_NEUE -> FontFamily(Font(R.font.bebas_neue))
        TripFont.ABRIL_FATFACE -> FontFamily(Font(R.font.abril_fatface))
        TripFont.PERMANENT_MARKER -> FontFamily(Font(R.font.permanent_marker))
    }

val TripFont.label: String
    get() = when (this) {
        TripFont.DEFAULT -> "Clásica"
        TripFont.PACIFICO -> "Pacifico"
        TripFont.LOBSTER -> "Lobster"
        TripFont.BEBAS_NEUE -> "Bebas Neue"
        TripFont.ABRIL_FATFACE -> "Abril Fatface"
        TripFont.PERMANENT_MARKER -> "Marker"
    }
