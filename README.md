# Calendario — Agenda académica para Android

Prototipo de aplicación móvil para consultar el horario de clases, revisar cambios de salón y acceder a información de ubicación dentro del campus. Está desarrollado en **Kotlin** con **Jetpack Compose** y **Material 3**, con interfaz en español.

## Estado de la entrega

> **El ZIP entregado está incompleto.** Incluye las pantallas de horario y detalle de clase, pero faltan componentes y clases que el código importa. Es necesario recuperar esos archivos antes de compilar y ejecutar la aplicación.

Este README describe el contenido de `calendario-EntregaFinal.zip`. La compilación y la ejecución no se han verificado.

## Funcionalidades

### Interfaces incluidas

- **Horario del día:** tarjeta de próxima clase con curso, horario, edificio y salón, además de próximas clases de ejemplo.
- **Avisos de cambio de salón:** presentación del mensaje, aula anterior y nuevo salón cuando se recibe una alerta.
- **Detalle de clase:** información del curso, docente, horario y destino actual.
- **Acceso al mapa:** botón «Ver cómo llegar» conectado al flujo de navegación de `MainActivity`.
- **Tema visual:** colores y tipografía compartidos mediante Material 3.

Las pantallas incluidas dependen de `CampusAlert`, cuya definición no está en el ZIP. La ubicación del detalle se representa con un marcador visual; ese componente no implementa un mapa interactivo.

### Flujos referenciados pendientes de completar

`MainActivity` contempla una pantalla de acceso y navegación entre **Horario**, **Mapa** y **Alertas**, además de la escucha de cambios mediante un repositorio. Las implementaciones de acceso, mapa, listado de alertas, barra inferior y capa de datos no están incluidas.

## Tecnologías y configuración

Los siguientes valores corresponden a la configuración incluida en el proyecto:

| Elemento | Versión o valor declarado |
| --- | --- |
| Lenguaje | Kotlin; plugin de Compose `2.2.10` |
| Interfaz | Jetpack Compose y Material 3 |
| Compose BOM | `2026.02.01` |
| Android Gradle Plugin | `9.3.3` |
| Gradle Wrapper | `9.5.0` |
| Android mínimo | Android 7.0, API `24` |
| SDK de compilación y destino | API `37` |
| JVM del daemon de Gradle | JDK `25` |
| Compatibilidad del código Java | Java `11` |
| Datos remotos previstos | Cloud Firestore; Firebase BOM `35.2.0` |
| Google Services | Plugin `4.6.0`, aplicado condicionalmente |
| Pruebas | JUnit 4, AndroidX Test y Espresso |
| Identificador de aplicación | `com.example.calendario` |
| Versión de la aplicación | `1.0` (`versionCode = 1`) |

La versión de JVM del daemon se define en `gradle/gradle-daemon-jvm.properties`; la compatibilidad Java 11 se declara por separado en `app/build.gradle.kts`.

## Requisitos

- Android Studio compatible con las versiones de Gradle y del plugin de Android declaradas.
- Android SDK Platform 37.
- JDK 25 disponible para el daemon, según la configuración del proyecto.
- Emulador o dispositivo físico con Android 7.0 (API 24) o superior.
- Conexión a Internet para descargar herramientas y dependencias durante la configuración inicial.
- Archivos fuente faltantes recuperados antes de intentar ejecutar la aplicación.

## Instalación y ejecución

1. Descomprime `calendario-EntregaFinal.zip`.
2. Abre Android Studio y selecciona **Open**.
3. Selecciona la carpeta `calendario-EntregaFinal`, que contiene `settings.gradle.kts`.
4. Recupera las implementaciones faltantes indicadas en la siguiente sección, respetando sus paquetes e interfaces de uso.
5. Configura el SDK y la JVM de Gradle, y sincroniza el proyecto.
6. Inicia un emulador o conecta un dispositivo con depuración USB habilitada.
7. Selecciona el módulo `app` y pulsa **Run**.

### Archivos fuente faltantes

Las rutas de esta tabla son relativas a `app/src/main/java/com/example/calendario/`. Los nombres de símbolos provienen de las referencias del código incluido; sus archivos concretos pueden tener otros nombres.

| Paquete o ruta | Símbolos que faltan |
| --- | --- |
| `data/` | `CampusAlert`, `AlertFeedState`, `AlertsRepository`, `scheduleClassroomAlert`, `resolveClassroomAlert` |
| `ui/components/` | `CampusBottomBar`, `CampusTab` |
| `ui/screens/` | `LoginScreen`, `MapScreen`, `AlertsScreen` |

Sin estas implementaciones, la compilación encontrará referencias sin resolver.

### Compilar desde la terminal

Una vez completados los archivos fuente y configurado el entorno, ejecuta los comandos desde la raíz del proyecto.

**Windows PowerShell:**

```powershell
.\gradlew.bat assembleDebug
```

**Linux o macOS:**

```bash
chmod +x gradlew
./gradlew assembleDebug
```

Si la compilación termina correctamente, el APK de depuración se generará en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Configuración de Firebase

El módulo `app` declara la dependencia de Cloud Firestore. El plugin de Google Services se aplica únicamente si existe el archivo `app/google-services.json`.

Para preparar la conexión con un proyecto Firebase:

1. Registra una aplicación Android con el identificador `com.example.calendario`.
2. Coloca su archivo `google-services.json` en la carpeta `app/`.
3. Recupera `AlertsRepository` y los modelos de datos faltantes.
4. Revisa en esa implementación las colecciones, los campos y los requisitos de acceso antes de configurar Firestore.
5. Sincroniza y recompila el proyecto.

El script de Gradle indica que se prevé un modo de maqueta sin Firebase, pero no es posible comprobar ese comportamiento con los archivos entregados. Tampoco se incluye el esquema de Firestore ni sus reglas de acceso. Agregar `google-services.json` por sí solo no resuelve los archivos fuente faltantes.

## Estructura principal

| Ruta | Contenido |
| --- | --- |
| `app/src/main/java/com/example/calendario/MainActivity.kt` | Punto de entrada, estado de acceso y coordinación de navegación y alertas |
| `app/src/main/java/com/example/calendario/ui/screens/ScheduleScreen.kt` | Interfaz del horario y próxima clase |
| `app/src/main/java/com/example/calendario/ui/screens/ClassDetailScreen.kt` | Detalle del curso, cambio de salón y destino |
| `app/src/main/java/com/example/calendario/ui/theme/` | Colores, tipografía y tema |
| `app/src/main/res/` | Textos, iconos y recursos Android |
| `app/src/main/AndroidManifest.xml` | Declaración de la aplicación, actividad principal y permiso de Internet |
| `app/src/test/` | Prueba unitaria de ejemplo |
| `app/src/androidTest/` | Prueba instrumentada de ejemplo |
| `app/build.gradle.kts` | Configuración del módulo y dependencias |
| `gradle/libs.versions.toml` | Catálogo de versiones |
| `gradle/wrapper/` | Configuración y ejecutable del Gradle Wrapper |

## Pruebas

Se incluyen dos pruebas de ejemplo: una comprueba una suma y otra valida el identificador del paquete en Android. No cubren los flujos de horario, navegación ni alertas.

Con el proyecto completo y el entorno configurado:

| Comprobación | Linux / macOS | Windows PowerShell |
| --- | --- | --- |
| Pruebas unitarias de depuración | `./gradlew testDebugUnitTest` | `.\gradlew.bat testDebugUnitTest` |
| Pruebas instrumentadas | `./gradlew connectedDebugAndroidTest` | `.\gradlew.bat connectedDebugAndroidTest` |

Las pruebas instrumentadas requieren un emulador activo o un dispositivo conectado. Estos comandos se documentan para su ejecución posterior; no se han ejecutado sobre esta entrega.

## Consideraciones del prototipo

- La fecha del horario, varias clases, sus horas y el nombre del docente están escritos directamente en las pantallas como datos de ejemplo.
- La pantalla de acceso está referenciada, pero su implementación y el mecanismo de autenticación no están disponibles.
- No se puede validar el funcionamiento del mapa ni la recepción de alertas hasta recuperar sus implementaciones.
- Antes de una entrega ejecutable, completa los archivos faltantes y verifica la sincronización, compilación y navegación en Android.
