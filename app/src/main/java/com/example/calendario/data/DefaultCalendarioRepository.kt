package com.example.calendario.data

import androidx.room.withTransaction
import com.example.calendario.data.local.*
import com.example.calendario.data.remote.*
import com.example.calendario.domain.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class DefaultCalendarioRepository(
    private val db: CalendarioDatabase
) : CalendarioRepository {

    private val firebaseApp: FirebaseApp
        get() = try {
            FirebaseApp.getInstance()
        } catch (e: IllegalStateException) {
            error("Firebase no está configurado. Agrega app/google-services.json y vuelve a compilar.")
        }

    private val firestore
        get() = FirebaseFirestore.getInstance(firebaseApp)

    private val auth
        get() = FirebaseAuth.getInstance(firebaseApp)

    private val authMutex = Mutex()

    override val historial =
        db.dao().observarHistorial().map { filas ->
            filas.map { fila ->
                EnvioConfirmado(
                    registro = Registro(
                        id = fila.id,
                        nombre = fila.nombre,
                        correo = fila.correo,
                        codigo = fila.codigo
                    ),
                    rutaFirebase = fila.rutaFirebase,
                    confirmadoEn = fila.confirmadoEn
                )
            }
        }

    private suspend fun obtenerUid(): String =
        authMutex.withLock {
            auth.currentUser?.uid
                ?: auth.signInAnonymously()
                    .await()
                    .user
                    ?.uid
                ?: error("No se pudo iniciar sesión en Firebase")
        }

    override suspend fun leerBorrador(): Registro? {
        return db.dao().leerBorrador()?.toDomain()
    }

    override suspend fun guardarBorrador(registro: Registro) {
        db.dao().guardarBorrador(
            BorradorEntity(
                id = registro.id,
                nombre = registro.nombre,
                correo = registro.correo,
                codigo = registro.codigo
            )
        )
    }

    override suspend fun enviar(registro: Registro): String {
        registro.validar()

        withTimeout(5_000) { guardarBorrador(registro) }

        val ruta = withTimeout(20_000) {
            val referencia = firestore
                .collection("usuarios")
                .document(obtenerUid())
                .collection("registros")
                .document(registro.id)

            referencia.set(
                mapOf(
                    "nombre" to registro.nombre,
                    "correo" to registro.correo,
                    "codigo" to registro.codigo,
                    "actualizadoEn" to FieldValue.serverTimestamp()
                )
            ).await()

            val confirmado = referencia
                .get(Source.SERVER)
                .await()
                .toRegistro()

            check(confirmado == registro) {
                "Los datos del servidor no coinciden."
            }

            referencia.path
        }

        withTimeout(5_000) {
            db.withTransaction {
                db.dao().guardarHistorial(
                    HistorialEntity(
                        id = registro.id,
                        nombre = registro.nombre,
                        correo = registro.correo,
                        codigo = registro.codigo,
                        rutaFirebase = ruta,
                        confirmadoEn = System.currentTimeMillis()
                    )
                )

                db.dao().eliminarBorrador()
            }
        }

        return ruta
    }

    override suspend fun leerRegistrosRemotos(): Lectura<List<Registro>> =
        leerConCache(hayDatos = { it.isNotEmpty() }) { source ->
            val uid = if (source == Source.CACHE) {
                auth.currentUser?.uid ?: error("No hay una sesión guardada para consultar sin conexión.")
            } else {
                obtenerUid()
            }
            firestore
                .collection("usuarios")
                .document(uid)
                .collection("registros")
                .get(source)
                .await()
                .documents
                .map { it.toRegistro() }
        }

    override suspend fun leerClases(): Lectura<List<Clase>> =
        leerConCache(hayDatos = { it.isNotEmpty() }) { source ->
            if (source == Source.SERVER) obtenerUid()

            firestore
                .collection("clases")
                .get(source)
                .await()
                .documents
                .map { it.toClase() }
                .sortedBy { it.id }
        }

    override suspend fun leerClase(classId: Int): Lectura<Clase?> =
        leerConCache(hayDatos = { it != null }) { source ->
            if (source == Source.SERVER) obtenerUid()
            val documento = firestore.collection("clases")
                .document(classId.toString())
                .get(source)
                .await()
            if (documento.exists()) documento.toClase() else null
        }

    private suspend fun <T> leerConCache(
        hayDatos: (T) -> Boolean,
        consulta: suspend (Source) -> T,
    ): Lectura<T> {
        try {
            return Lectura(withTimeout(10_000) { consulta(Source.SERVER) })
        } catch (e: TimeoutCancellationException) {
            // El límite de red deja tiempo para consultar la copia local.
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val problemaDeConexion = e is FirebaseNetworkException ||
                (e is FirebaseFirestoreException && e.code in setOf(
                    FirebaseFirestoreException.Code.UNAVAILABLE,
                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
                ))
            if (!problemaDeConexion) throw e
        }

        val datos = withTimeout(4_000) { consulta(Source.CACHE) }
        check(hayDatos(datos)) {
            "Sin conexión y sin datos guardados. Conéctate y reintenta."
        }
        return Lectura(datos, desdeCache = true)
    }
}
