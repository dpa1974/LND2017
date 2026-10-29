# Cuenta atrás de viaje (Android)

App nativa en Kotlin + Jetpack Compose (Material 3) que muestra una cuenta atrás hasta un viaje.

## Funciones
- Varios viajes: se pasa de uno a otro deslizando (ordenados por fecha). **+** añade, el lápiz edita,
  y desde el editor se puede borrar.
- Texto personalizable (título del viaje, hasta 60 caracteres).
- Tipo de letra por viaje para el título y los números: Clásica, Pacifico, Lobster, Bebas Neue,
  Abril Fatface y Permanent Marker (Google Fonts incluidas en la app; licencias en `licenses/fonts`).
  El widget usa siempre la del sistema: los widgets no admiten fuentes propias.
- Fecha y hora del viaje (no deja elegir días pasados).
- Color del texto: 10 colores rápidos + selector HSV.
- Imagen de fondo desde la galería (Photo Picker, sin pedir permisos). Se copia a almacenamiento
  interno para que siga ahí tras reiniciar el móvil.
- Deslizador para oscurecer el fondo (0–80 %) y que el texto se lea sobre cualquier foto.
- Widget de pantalla de inicio: días y horas que faltan, con la foto y el color del viaje. Al añadirlo
  se elige qué viaje muestra; tocarlo abre la app en ese viaje. Se refresca cada 30 min y al guardar.
- Recordatorio diario (campana 🔔): notificación a la hora elegida con los días que faltan para cada
  viaje, también con el móvil bloqueado. Puede llegar con unos minutos de margen (alarma inexacta, no
  necesita el permiso de alarmas exactas). Se restaura sola tras reiniciar el móvil.
- Los cambios se ven en vivo y solo se guardan al pulsar **Guardar**.

## Estructura
| Fichero | Qué hace |
|---|---|
| `Countdown.kt` | Cálculo puro del tiempo restante (con test en `src/test`). |
| `data/Trip.kt` | Modelo de un viaje. |
| `data/TripRepository.kt` | Lista de viajes en DataStore (JSON) y gestión de las imágenes copiadas. |
| `ui/CountdownViewModel.kt` | Estado guardado + borrador de edición. |
| `ui/CountdownScreen.kt` | Paginador de viajes, refresco cada segundo. |
| `ui/EditSheet.kt`, `ui/ColorPicker.kt` | Panel de personalización y selectores. |
| `widget/TripWidget.kt` | Widget con Jetpack Glance. |
| `widget/TripWidgetConfigActivity.kt` | Pantalla para elegir el viaje del widget. |
| `reminder/BackgroundAccess.kt` | Excepción de batería y pantalla de inicio automático del fabricante. |
| `ui/Fonts.kt` | Fuentes incluidas y su nombre visible. |
| `reminder/ReminderScheduler.kt` | Programa la alarma del recordatorio diario. |
| `reminder/ReminderReceiver.kt` | Muestra la notificación y reprograma (también tras reiniciar). |
| `ui/ReminderDialog.kt` | Diálogo para activar el recordatorio y elegir la hora. |

## Compilar
Abrir la carpeta `CuentaAtrasViaje` en Android Studio (Ladybug o posterior) y ejecutar.
Por terminal: `./gradlew assembleDebug` y `./gradlew test`.
Requisitos: JDK 17+, Android SDK 35. minSdk 26 (Android 8.0).

## Notas para móviles OPPO / Xiaomi / Huawei
Estas capas cierran apps en segundo plano. El diálogo del recordatorio incluye «Para que el aviso llegue
siempre»: pide quitar la optimización de batería y abre la pantalla de inicio automático del fabricante
(o la ficha de la app si esa pantalla no existe en esa versión). Para instalar
desde Android Studio en OPPO hay que activar «Instalar vía USB» en Opciones de desarrollador.

## Generar el APK para compartir
Build → Generate Signed App Bundle or APK → **APK** → keystore (`.jks`) → variante `release`.
El resultado queda en `app/release/app-release.apk`. Guarda el `.jks` y sus contraseñas: sin ellos no
se pueden publicar actualizaciones que se instalen encima.
