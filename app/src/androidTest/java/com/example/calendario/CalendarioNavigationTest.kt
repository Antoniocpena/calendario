package com.example.calendario

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.calendario.domain.*
import com.example.calendario.navigation.CalendarioApp
import com.example.calendario.ui.theme.CalendarioTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarioNavigationTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun navegaEntreHorarioDetalleYFormularioYGuardaUnBorrador() {
        val repository = NavigationRepository()
        compose.setContent {
            CalendarioTheme { CalendarioApp(repository) }
        }
        compose.onNodeWithText("Ver detalle").performScrollTo().performClick()
        compose.onNodeWithText("Detalle de la clase").assertIsDisplayed()
        compose.onNodeWithText("MAT101 · MAT").assertIsDisplayed()
        compose.onNodeWithContentDescription("Regresar").performClick()
        compose.onNodeWithText("Perfil").performClick()
        compose.onNodeWithText("Formulario").assertIsDisplayed()
        compose.onNodeWithText("Nombre").performTextInput("Ana")
        compose.onNodeWithText("Correo").performTextInput("ana@example.com")
        compose.onNodeWithText("Código de estudiante").performTextInput("ABC123")
        compose.onNodeWithText("Guardar borrador").performScrollTo().performClick()
        compose.onNodeWithText("Borrador guardado en este dispositivo.").performScrollTo().assertIsDisplayed()
        compose.runOnIdle {
            assertEquals("Ana", repository.borrador?.nombre)
            assertEquals("ana@example.com", repository.borrador?.correo)
            assertEquals("ABC123", repository.borrador?.codigo)
        }
    }

    private class NavigationRepository : CalendarioRepository {
        override val historial = flowOf(emptyList<EnvioConfirmado>())
        var borrador: Registro? = null
        private val clase = Clase(
            7, "Lunes", "08:00", "09:00",
            Materia("MAT101", "Matemáticas", "MAT"),
            Salon("101", Edificio("Edificio A", "Entrada norte")),
            Catedratico("Docente", "docente@example.com"),
        )
        override suspend fun leerBorrador() = borrador
        override suspend fun guardarBorrador(registro: Registro) { borrador = registro }
        override suspend fun enviar(registro: Registro): String = error("Esta prueba no envía datos a Firebase")
        override suspend fun leerRegistrosRemotos() = Lectura(emptyList<Registro>())
        override suspend fun leerClases() = Lectura(listOf(clase))
        override suspend fun leerClase(classId: Int) = Lectura(clase.takeIf { it.id == classId })
    }
}
