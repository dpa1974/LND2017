package com.dpa.cuentaatras.ui

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dpa.cuentaatras.data.TripRepository
import com.dpa.cuentaatras.data.TripSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Mientras el editor está abierto se trabaja sobre [draft]; la pantalla lo pinta en vivo
 * detrás del panel, y solo se persiste al pulsar Guardar.
 */
class CountdownViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = TripRepository(app)

    val settings: StateFlow<TripSettings?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    var draft by mutableStateOf<TripSettings?>(null)
        private set

    var imageError by mutableStateOf(false)
        private set

    fun startEditing() {
        draft = settings.value
    }

    fun updateDraft(settings: TripSettings) {
        draft = settings
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
            draft = null
        }
    }

    fun cancel() {
        draft = null
        viewModelScope.launch { repository.cleanupBackgrounds(keep = settings.value?.backgroundPath) }
    }
}
