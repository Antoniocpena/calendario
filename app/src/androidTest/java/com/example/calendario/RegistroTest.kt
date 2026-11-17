package com.example.calendario

import com.example.calendario.domain.Registro
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistroTest {

    @Test
    fun aceptaRegistroValido() {
        Registro(
            id = "1",
            nombre = "Ana",
            correo = "ana@example.com",
            codigo = "ABC123"
        ).validar()
    }

    @Test
    fun rechazaDatosInvalidos() {
        val invalidos = listOf(
            Registro(
                id = "1",
                nombre = "",
                correo = "ana@example.com",
                codigo = "ABC123"
            ),
            Registro(
                id = "2",
                nombre = "Ana",
                correo = "ana@",
                codigo = "ABC123"
            ),
            Registro(
                id = "3",
                nombre = "Ana",
                correo = "ana@example.com",
                codigo = "AB"
            )
        )

        invalidos.forEach { registro ->
            assertTrue(
                runCatching {
                    registro.validar()
                }.exceptionOrNull() is IllegalArgumentException
            )
        }
    }
}