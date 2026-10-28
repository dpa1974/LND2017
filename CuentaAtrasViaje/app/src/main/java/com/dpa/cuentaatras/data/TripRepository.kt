package com.dpa.cuentaatras.data

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dpa.cuentaatras.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore(name = "trip")

/** Guarda la lista de viajes como JSON en DataStore; son pocos y siempre se leen enteros. */
class TripRepository(private val context: Context) {

    private val backgroundsDir get() = File(context.filesDir, "backgrounds")

    /** Viajes ordenados por fecha. Nunca está vacía: sin datos guardados emite un viaje por defecto. */
    val trips: Flow<List<Trip>> = context.dataStore.data.map { prefs ->
        prefs[TRIPS]?.let(::parseTrips)?.takeIf { it.isNotEmpty() }?.sortedBy { it.targetMillis }
            ?: listOf(newTrip(id = DEFAULT_ID))
    }

    fun newTrip(id: String = UUID.randomUUID().toString()) = Trip(
        id = id,
        title = context.getString(R.string.default_title),
        targetMillis = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30),
        textColor = Trip.DEFAULT_TEXT_COLOR,
        backgroundPath = null,
        dim = Trip.DEFAULT_DIM,
    )

    /** Inserta o reemplaza el viaje con el mismo id. */
    suspend fun save(trip: Trip) = update { trips -> trips.filter { it.id != trip.id } + trip }

    suspend fun delete(id: String) = update { trips -> trips.filter { it.id != id } }

    private suspend fun update(transform: (List<Trip>) -> List<Trip>) {
        var result = emptyList<Trip>()
        context.dataStore.edit { prefs ->
            result = transform(prefs[TRIPS]?.let(::parseTrips).orEmpty())
            prefs[TRIPS] = result.toJson()
        }
        cleanupBackgrounds(keep = result.mapNotNull { it.backgroundPath }.toSet())
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

    /** Borra las imágenes importadas que no usa ningún viaje guardado. */
    suspend fun cleanupBackgrounds(keep: Set<String>) = withContext(Dispatchers.IO) {
        backgroundsDir.listFiles()
            ?.filter { it.absolutePath !in keep }
            ?.forEach { it.delete() }
    }

    private fun parseTrips(json: String): List<Trip> = runCatching {
        val array = JSONArray(json)
        (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            Trip(
                id = o.getString("id"),
                title = o.getString("title"),
                targetMillis = o.getLong("target"),
                textColor = o.getInt("color"),
                backgroundPath = if (o.isNull("bg")) null else o.getString("bg").takeIf { File(it).exists() },
                dim = o.optDouble("dim", Trip.DEFAULT_DIM.toDouble()).toFloat(),
            )
        }
    }.getOrDefault(emptyList())

    private fun List<Trip>.toJson(): String = JSONArray().also { array ->
        forEach { t ->
            array.put(
                JSONObject()
                    .put("id", t.id)
                    .put("title", t.title)
                    .put("target", t.targetMillis)
                    .put("color", t.textColor)
                    .put("bg", t.backgroundPath ?: JSONObject.NULL)
                    .put("dim", t.dim.toDouble())
            )
        }
    }.toString()

    private companion object {
        val TRIPS = stringPreferencesKey("trips")
        const val DEFAULT_ID = "default"
    }
}
