# Calendario Campus

Aplicación Android para consultar el horario de clases del día y recibir avisos en tiempo real cuando cambia el salón de una clase. Desarrollada con Kotlin y Jetpack Compose, con Firebase Firestore como fuente de las alertas.

Proyecto del curso Desarrollo de Aplicaciones Móviles, entrega final. Equipo: Balanceados.

## Características

* Inicio de sesión como puerta de entrada a la app.
* Horario del día: muestra la próxima clase y la lista de clases siguientes con su ubicación y hora.
* Detalle de la clase: curso, docente, horario, edificio y salón actual.
* Alertas de cambio de salón: resalta el salón original y el nuevo salón, tanto en el horario como en el detalle.
* Mapa del campus: acceso directo desde el detalle con el botón "Ver cómo llegar".
* Pantalla de alertas: feed de avisos sincronizado con Firestore, con opción de reintentar si falla la conexión.
* Navegación inferior entre Horario, Mapa y Alertas, con soporte del botón atrás.
* Modo maqueta: la app funciona sin Firebase usando una alerta de ejemplo (`scheduleClassroomAlert`).

## Tecnologías

* Kotlin 2.2.10
* Jetpack Compose con Material 3 (BOM 2026.02.01)
* Firebase Firestore (BOM 35.2.0)
* Gradle 9.5.0 y Android Gradle Plugin 9.3.3
* minSdk 24, targetSdk 37, compileSdk 37

## Estructura del proyecto

```
calendario/
├── app/
│   └── src/main/java/com/example/calendario/
│       ├── MainActivity.kt          # Punto de entrada, login y navegación
│       ├── data/                    # AlertsRepository, CampusAlert, AlertFeedState
│       └── ui/
│           ├── components/          # CampusBottomBar, CampusTab
│           ├── screens/             # Login, Horario, Detalle, Mapa y Alertas
│           └── theme/               # Color.kt, Type.kt, Theme.kt
├── gradle/libs.versions.toml        # Catálogo de versiones
├── build.gradle.kts
└── settings.gradle.kts
```

## Cómo ejecutarlo

Requisitos:

* Android Studio reciente, compatible con AGP 9.3.x
* JDK 25 (Gradle puede descargarlo automáticamente)
* Un emulador o dispositivo con Android 7.0 (API 24) o superior

Pasos:

1. Clona el repositorio: `git clone <URL-DEL-REPOSITORIO>`
2. Abre la carpeta en Android Studio y espera a que termine la sincronización de Gradle.
3. Selecciona un emulador o dispositivo.
4. Presiona Run, o ejecuta `./gradlew installDebug` desde la terminal.

La app compila y corre sin configuración adicional en modo maqueta.

## Configuración de Firebase (opcional)

Para recibir alertas reales desde Firestore:

1. Crea un proyecto en la consola de Firebase y habilita Cloud Firestore.
2. Registra una app Android con el package name `com.example.calendario`.
3. Descarga `google-services.json` y colócalo en la carpeta `app/`.
4. Sincroniza Gradle y vuelve a compilar. El plugin `google-services` se aplica automáticamente cuando el archivo existe.

## Pruebas

```
./gradlew test                    # Pruebas unitarias
./gradlew connectedAndroidTest    # Pruebas instrumentadas (requiere dispositivo o emulador)
```

## Equipo

Balanceados

## Licencia

