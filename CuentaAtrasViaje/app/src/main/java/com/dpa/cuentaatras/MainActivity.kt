package com.dpa.cuentaatras

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.dpa.cuentaatras.ui.AppTheme
import com.dpa.cuentaatras.ui.CountdownScreen
import com.dpa.cuentaatras.ui.CountdownViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CountdownViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Iconos de barra de estado claros: siempre van sobre una imagen o degradado oscurecido.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        // Al abrir desde el widget, ir directamente a ese viaje.
        if (savedInstanceState == null) intent.getStringExtra(EXTRA_TRIP_ID)?.let(viewModel::showTrip)
        setContent {
            AppTheme { CountdownScreen(viewModel) }
        }
    }

    companion object {
        const val EXTRA_TRIP_ID = "trip_id"
    }
}
