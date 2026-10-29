package com.dpa.cuentaatras.data

/** Tipos de letra disponibles para el título y los números. Se guardan por [name]. */
enum class TripFont {
    DEFAULT, PACIFICO, LOBSTER, BEBAS_NEUE, ABRIL_FATFACE, PERMANENT_MARKER;

    companion object {
        fun fromName(name: String?) = entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
