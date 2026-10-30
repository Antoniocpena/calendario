package com.example.calendario

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.calendario.data.local.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomPersistenceTest {

    @Test
    fun datosPersistenTrasCerrarYReabrir() = runBlocking {
        val context =
            ApplicationProvider.getApplicationContext<Context>()

        val nombreDb = "persistencia-test.db"

        context.deleteDatabase(nombreDb)

        val borrador = BorradorEntity(
            id = "draft-1",
            nombre = "Prueba local",
            correo = "prueba@example.com",
            codigo = "ABC123"
        )

        val envio = HistorialEntity(
            id = "remote-1",
            nombre = "Prueba",
            correo = "prueba@example.com",
            codigo = "ABC123",
            rutaFirebase =
                "usuarios/test/registros/remote-1",
            confirmadoEn = 123L
        )

        try {
            val primera = CalendarioDatabase.abrir(
                context = context,
                nombre = nombreDb
            )

            try {
                primera.dao().guardarBorrador(borrador)
                primera.dao().guardarHistorial(envio)
            } finally {
                primera.close()
            }

            val segunda = CalendarioDatabase.abrir(
                context = context,
                nombre = nombreDb
            )

            try {
                assertEquals(
                    borrador,
                    segunda.dao().leerBorrador()
                )

                assertEquals(
                    listOf(envio),
                    segunda.dao()
                        .observarHistorial()
                        .first()
                )

                segunda.dao().eliminarBorrador()

                assertNull(
                    segunda.dao().leerBorrador()
                )

                assertEquals(
                    listOf(envio),
                    segunda.dao()
                        .observarHistorial()
                        .first()
                )
            } finally {
                segunda.close()
            }
        } finally {
            context.deleteDatabase(nombreDb)
        }
    }
}