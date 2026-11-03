package com.example.calendario.domain

import kotlinx.coroutines.flow.Flow

data class EnvioConfirmado(
    val registro: Registro,
    val rutaFirebase: String,
    val confirmadoEn: Long
)

interface CalendarioRepository {

    val historial: Flow<List<EnvioConfirmado>>

    suspend fun leerBorrador(): Registro?

    suspend fun guardarBorrador(registro: Registro)

    suspend fun enviar(registro: Registro): String

    suspend fun leerRegistrosRemotos(): Lectura<List<Registro>>

    suspend fun leerClases(): Lectura<List<Clase>>

    suspend fun leerClase(classId: Int): Lectura<Clase?>
}
