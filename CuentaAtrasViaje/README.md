# Cuenta atrás de viaje (Android)

App nativa en Kotlin + Jetpack Compose (Material 3) que muestra una cuenta atrás hasta un viaje.

## Funciones
- Texto personalizable (título del viaje, hasta 60 caracteres).
- Fecha y hora del viaje (no deja elegir días pasados).
- Color del texto: 10 colores rápidos + selector HSV.
- Imagen de fondo desde la galería (Photo Picker, sin pedir permisos). Se copia a almacenamiento
  interno para que siga ahí tras reiniciar el móvil.
- Los cambios se ven en vivo y solo se guardan al pulsar **Guardar**.

## Estructura
| Fichero | Qué hace |
|---|---|
| `Countdown.kt` | Cálculo puro del tiempo restante (con test en `src/test`). |
| `data/TripRepository.kt` | Persistencia con DataStore y gestión de las imágenes copiadas. |
| `ui/CountdownViewModel.kt` | Estado guardado + borrador de edición. |
| `ui/CountdownScreen.kt` | Pantalla principal, refresco cada segundo. |
| `ui/EditSheet.kt`, `ui/ColorPicker.kt` | Panel de personalización y selectores. |

## Compilar
Abrir la carpeta `CuentaAtrasViaje` en Android Studio (Ladybug o posterior) y ejecutar.
Por terminal: `./gradlew assembleDebug` y `./gradlew test`.
Requisitos: JDK 17+, Android SDK 35. minSdk 26 (Android 8.0).
