# Calendario Campus

Aplicación Android en Kotlin y Jetpack Compose para consultar clases y registrar datos de estudiantes. Proyecto del curso Desarrollo de Aplicaciones Móviles, entrega final. Equipo: Balanceados.

## Funciones disponibles

* Horario y detalle de clases consultados desde Firebase Firestore.
* Formulario con validación de nombre, correo y código de estudiante.
* Borrador e historial de envíos confirmados guardados en Room.
* Envío de registros con autenticación anónima, confirmación de lectura desde el servidor y reintentos.
* Consulta de registros del usuario y recuperación de datos de Firestore desde caché cuando falla la conexión.
* Navegación entre horario, detalle y formulario, conservando los campos al recrear la pantalla.

Los accesos a Mapa, Alertas y "Ver cómo llegar" son elementos pendientes y actualmente están deshabilitados.

## Requisitos

* Android Studio compatible con Android Gradle Plugin 9.3.3.
* JDK 25; el proyecto incluye la configuración para descargarlo mediante Gradle.
* Android SDK 37.
* Emulador o dispositivo con Android 7.0 (API 24) o superior.

El proyecto usa Gradle 9.5.0, Jetpack Compose, Navigation, Room 2.8.5 y Firebase BOM 34.7.0. Las dependencias se declaran en `app/build.gradle.kts` y `gradle/libs.versions.toml`.

## Ejecución

1. Abre esta carpeta en Android Studio.
2. Instala el SDK solicitado y espera a que termine la sincronización de Gradle.
3. Selecciona un dispositivo y ejecuta la configuración `app`.

Desde PowerShell en la carpeta del proyecto:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
```

El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## Firebase

La configuración Android se carga desde `app/google-services.json`, con el identificador de aplicación `com.example.calendario`.

Para usar un proyecto Firebase propio:

1. Registra la aplicación Android y coloca su archivo `google-services.json` en `app/`.
2. Habilita Authentication con el proveedor Anónimo.
3. Crea Cloud Firestore y configura reglas que permitan las lecturas necesarias y limiten los registros de cada usuario a su UID autenticado.
4. Carga los documentos de clases y vuelve a compilar.

Colecciones utilizadas:

* `clases/{classId}`: el ID debe ser un entero. Campos: `dia`, `horaInicio`, `horaFin`; objetos `materia` (`codigo`, `nombre`, `siglas`), `salon` (`numero`, `edificio` con `nombre` y `ubicacion`) y `catedratico` (`nombre`, `correo`). Todos esos valores deben ser cadenas no vacías.
* `usuarios/{uid}/registros/{registroId}`: campos `nombre`, `correo`, `codigo` y `actualizadoEn`.

Sin `google-services.json`, la aplicación compila y permite trabajar con los borradores y el historial local. Las operaciones remotas muestran un error de configuración; no se generan datos de ejemplo ni se simulan confirmaciones de envío. La caché de Firestore requiere haber descargado los datos previamente.

## Estructura

```text
app/src/main/java/com/example/calendario/
├── CalendarioApplication.kt    # Contenedor y repositorio compartido
├── MainActivity.kt             # Punto de entrada
├── data/                       # Repositorio, Room y conversión de Firestore
├── domain/                     # Modelos y contrato del repositorio
├── navigation/                 # Rutas y navegación Compose
└── ui/
    ├── components/             # Carga, errores y avisos de caché
    ├── screens/                # Horario, detalle y formulario
    ├── state/                  # Estados de pantalla
    ├── viewmodel/              # Consultas, borradores y envíos
    └── theme/                  # Tema visual
```

## Verificación

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

Las pruebas unitarias cubren reintentos, recuperación de borradores, restauración de campos, envíos duplicados, tiempos de espera y datos de caché. Las pruebas instrumentadas comprueban la navegación y la persistencia de Room; requieren un dispositivo conectado. Las pruebas de navegación utilizan un repositorio de prueba y no envían registros a Firebase.
