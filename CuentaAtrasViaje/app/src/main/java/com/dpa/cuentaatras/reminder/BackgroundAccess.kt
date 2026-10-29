package com.dpa.cuentaatras.reminder

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * Ayuda a que el recordatorio llegue aunque la app lleve días sin abrirse. Hay dos frenos distintos:
 * la optimización de batería de Android (se puede pedir una excepción con un diálogo del sistema) y el
 * "inicio automático" de capas como ColorOS, MIUI o EMUI, que no tiene API pública: se intenta abrir su
 * pantalla y, si no existe en esa versión, la ficha de la app en Ajustes.
 */
object BackgroundAccess {

    fun isUnrestricted(context: Context): Boolean =
        context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName)

    @SuppressLint("BatteryLife") // app de distribución directa: el aviso de Play no aplica
    fun requestUnrestricted(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
        if (!tryStart(context, intent)) tryStart(context, Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
    }

    /** true si el fabricante suele tener un gestor propio de inicio automático. */
    val hasVendorAutostart: Boolean get() = vendorAutostartScreens().isNotEmpty()

    fun openAutostartSettings(context: Context) {
        if (vendorAutostartScreens().any { tryStart(context, Intent().setComponent(it)) }) return
        tryStart(context, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
    }

    private fun vendorAutostartScreens(): List<ComponentName> = when (Build.MANUFACTURER.lowercase()) {
        "oppo", "realme", "oneplus" -> listOf(
            ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
            ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
            ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),
        )
        "xiaomi", "redmi", "poco" -> listOf(
            ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
        )
        "huawei", "honor" -> listOf(
            ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
            ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"),
        )
        "vivo" -> listOf(
            ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
        )
        else -> emptyList()
    }

    /** Las pantallas de fabricante cambian entre versiones: puede no existir o no estar exportada. */
    private fun tryStart(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: Exception) {
        false
    }
}
