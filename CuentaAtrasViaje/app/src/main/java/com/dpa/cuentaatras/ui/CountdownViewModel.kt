package com.dpa.cuentaatras.ui

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dpa.cuentaatras.data.Reminder
import com.dpa.cuentaatras.data.Trip
import com.dpa.cuentaatras.data.TripRepository
import com.dpa.cuentaatras.reminder.ReminderScheduler
import com.dpa.cuentaatras.widget.TripWidget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Mientras el editor está abierto se trabaja sobre [draft]; la pantalla lo pinta en vivo
 * detrás del panel, y solo se persiste al pulsar Guardar.
 */
class CountdownViewModel(private val app: Application) : AndroidViewModel(app) {

    private val repository = TripRepository(app)

    val trips: StateFlow<List<Trip>?> = repository.trips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val reminder: StateFlow<Reminder?> = repository.reminder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // Reprograma por si la alarma se perdió (p. ej. el usuario forzó el cierre de la app).
        viewModelScope.launch { ReminderScheduler.apply(app, repository.reminder.first()) }
    }

    var draft by mutableStateOf<Trip?>(null)
        private set

    /** true si [draft] es un viaje nuevo que aún no está en [trips]. */
    var isNewDraft by mutableStateOf(false)
        private set

    /** Viaje al que la pantalla debe desplazarse (recién guardado o abierto desde el widget). */
    var pageToShow by mutableStateOf<String?>(null)
        private set

    var imageError by mutableStateOf(false)
        private set

    fun startEditing(trip: Trip) {
        draft = trip
        isNewDraft = false
    }

    fun startNew() {
        draft = repository.newTrip()
        isNewDraft = true
    }

    fun updateDraft(trip: Trip) {
        draft = trip
    }

    fun showTrip(id: String) {
        pageToShow = id
    }

    fun pageShown() {
        pageToShow = null
    }

    fun importBackground(uri: Uri) {
        viewModelScope.launch {
            runCatching { repository.importBackground(uri) }
                .onSuccess { path -> draft = draft?.copy(backgroundPath = path) }
                .onFailure { imageError = true }
        }
    }

    fun imageErrorShown() {
        imageError = false
    }

    fun save() {
        val toSave = draft ?: return
        viewModelScope.launch {
            repository.save(toSave)
            pageToShow = toSave.id // al cambiar la fecha el viaje puede moverse de posición
            draft = null
            TripWidget().updateAll(app)
        }
    }

    fun delete() {
        val toDelete = draft ?: return
        draft = null
        viewModelScope.launch {
            repository.delete(toDelete.id)
            TripWidget().updateAll(app)
        }
    }

    fun saveReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.saveReminder(reminder)
            ReminderScheduler.apply(app, reminder)
        }
    }

    fun cancel() {
        draft = null
        val keep = trips.value.orEmpty().mapNotNull { it.backgroundPath }.toSet()
        viewModelScope.launch { repository.cleanupBackgrounds(keep) }
    }
}
