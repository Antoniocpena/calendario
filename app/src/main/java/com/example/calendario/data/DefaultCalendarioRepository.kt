package com.example.calendario.data

import androidx.room.withTransaction
import com.example.calendario.data.local.*
import com.example.calendario.data.remote.*
import com.example.calendario.domain.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class DefaultCalendarioRepository(
    private val db: CalendarioDatabase
) : CalendarioRepository {

    private val firestore
        get() = FirebaseFirestore.getInstance()

    private val auth
        get() = FirebaseAuth.getInstance()

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

        guardarBorrador(registro)

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

        return ruta
    }

    override suspend fun leerRegistrosRemotos(): List<Registro> =
        withTimeout(20_000) {
            firestore
                .collection("usuarios")
                .document(obtenerUid())
                .collection("registros")
                .get(Source.SERVER)
                .await()
                .documents
                .map { it.toRegistro() }
        }

    override suspend fun leerClases(): List<Clase> =
        withTimeout(20_000) {
            obtenerUid()

            firestore
                .collection("clases")
                .get(Source.SERVER)
                .await()
                .documents
                .map { it.toClase() }
                .sortedBy { it.id }
        }
}