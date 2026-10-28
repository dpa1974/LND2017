package com.dpa.cuentaatras.data

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dpa.cuentaatras.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore(name = "trip")

class TripRepository(private val context: Context) {

    private val backgroundsDir get() = File(context.filesDir, "backgrounds")

    val settings: Flow<TripSettings> = context.dataStore.data.map { prefs ->
        TripSettings(
            title = prefs[TITLE] ?: context.getString(R.string.default_title),
            targetMillis = prefs[TARGET] ?: (System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30)),
            textColor = prefs[TEXT_COLOR] ?: DEFAULT_TEXT_COLOR,
            backgroundPath = prefs[BACKGROUND]?.takeIf { File(it).exists() },
        )
    }

    suspend fun save(settings: TripSettings) {
        context.dataStore.edit { prefs ->
            prefs[TITLE] = settings.title
            prefs[TARGET] = settings.targetMillis
            prefs[TEXT_COLOR] = settings.textColor
            if (settings.backgroundPath != null) prefs[BACKGROUND] = settings.backgroundPath
            else prefs.remove(BACKGROUND)
        }
        cleanupBackgrounds(keep = settings.backgroundPath)
    }

    /**
     * Copia la imagen elegida a almacenamiento interno. Así no dependemos de que el
     * permiso sobre el Uri del selector siga vigente tras reiniciar la app.
     * Cada importación usa un nombre nuevo para que Coil no sirva la imagen anterior de caché.
     */
    suspend fun importBackground(uri: Uri): String = withContext(Dispatchers.IO) {
        backgroundsDir.mkdirs()
        val file = File(backgroundsDir, "bg_${System.currentTimeMillis()}.img")
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IOException("No se puede abrir $uri")
        input.use { src -> file.outputStream().use { dst -> src.copyTo(dst) } }
        file.absolutePath
    }

    /** Borra las imágenes importadas que ya no usa la configuración guardada. */
    suspend fun cleanupBackgrounds(keep: String?) = withContext(Dispatchers.IO) {
        backgroundsDir.listFiles()
            ?.filter { it.absolutePath != keep }
            ?.forEach { it.delete() }
    }

    private companion object {
        val TITLE = stringPreferencesKey("title")
        val TARGET = longPreferencesKey("target_millis")
        val TEXT_COLOR = intPreferencesKey("text_color")
        val BACKGROUND = stringPreferencesKey("background_path")
        const val DEFAULT_TEXT_COLOR = 0xFFFFFFFF.toInt()
    }
}
