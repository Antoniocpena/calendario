package com.example.calendario

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.calendario.data.CampusAlert
import com.example.calendario.data.CampusCache
import com.example.calendario.data.CampusRepository
import com.example.calendario.data.PRIMARY_ALERT_ID
import com.example.calendario.data.SaveOutcome
import com.example.calendario.data.SyncPhase
import com.example.calendario.data.SyncState
import com.example.calendario.data.UserProfile
import com.example.calendario.ui.theme.CalendarTheme
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CampusNavigationTest {
    @get:Rule val compose = createComposeRule()

    private fun start(repository: FakeRepository = FakeRepository()) {
        compose.setContent { CalendarTheme { CalendarApp(repository) } }
    }

    private fun signIn() {
        compose.onNodeWithText("Nombre").performTextInput("Alex")
        compose.onNodeWithText("Correo electrónico").performTextInput("alex@example.com")
        compose.onNodeWithText("Contraseña").performTextInput("test123")
        compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
        compose.onNodeWithText("Mi horario de hoy").assertIsDisplayed()
    }

    @Test
    fun loginValidatesThreeFieldsBeforeEnablingSubmit() {
        start()
        compose.onNodeWithText("Iniciar sesión").assertIsNotEnabled()
        compose.onNodeWithText("Nombre").performTextInput("A")
        compose.onNodeWithText("Usa al menos 2 caracteres.").assertIsDisplayed()
        compose.onNodeWithText("Nombre").performTextClearance()
        compose.onNodeWithText("Nombre").performTextInput("Alex")
        compose.onNodeWithText("Correo electrónico").performTextInput("broken")
        compose.onNodeWithText("Ingresa un correo electrónico válido.").assertIsDisplayed()
        compose.onNodeWithText("Correo electrónico").performTextClearance()
        compose.onNodeWithText("Correo electrónico").performTextInput("alex@example.com")
        compose.onNodeWithText("Contraseña").performTextInput("123")
        compose.onNodeWithText("Usa al menos 6 caracteres.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Iniciar sesión").assertIsNotEnabled()
        compose.onNodeWithText("Contraseña").performTextClearance()
        compose.onNodeWithText("Contraseña").performTextInput("123456")
        compose.onNodeWithText("Iniciar sesión").assertIsEnabled()
    }

    @Test
    fun detailReceivesAnAlertIdAndBackReturnsToAlerts() {
        start()
        signIn()
        compose.onNodeWithText("Alertas").performClick()
        compose.onNodeWithText("Ver detalle").performScrollTo().performClick()
        compose.onNodeWithText("Detalle de la alerta").assertIsDisplayed()
        compose.onNodeWithText("Aviso de laboratorio").assertIsDisplayed()
        compose.onNodeWithContentDescription("Regresar").performClick()
        compose.onNodeWithText("Tus alertas").assertIsDisplayed()
        compose.onNodeWithText("Ver detalle").performScrollTo().performClick()
        compose.onNodeWithText("Ver cómo llegar").performScrollTo().performClick()
        compose.onNodeWithText("Mapa del campus").assertIsDisplayed()
    }

    @Test
    fun scheduleTabReturnsHomeAfterDirections() {
        val repository = FakeRepository()
        repository.cache.update { cache ->
            cache.copy(alerts = listOf(cache.alerts.single().copy(id = PRIMARY_ALERT_ID)))
        }
        start(repository)
        signIn()
        compose.onNodeWithText("Ver detalle").performScrollTo().performClick()
        compose.onNodeWithText("Detalle de la alerta").assertIsDisplayed()
        compose.onNodeWithText("Ver cómo llegar").performScrollTo().performClick()
        compose.onNodeWithText("Mapa del campus").assertIsDisplayed()
        compose.onNodeWithText("Horario").performClick()
        compose.onNodeWithText("Mi horario de hoy").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Detalle de la alerta").assertDoesNotExist()
    }
    @Test
    fun savingAnAlertCallsTheRepositoryAndUpdatesTheList() {
        val repository = FakeRepository()
        start(repository)
        signIn()
        compose.onNodeWithText("Alertas").performClick()
        compose.onNodeWithText("Nueva alerta").performClick()
        compose.onNodeWithText("Título").performTextInput("Aviso de examen")
        compose.onNodeWithText("Mensaje").performTextInput("El examen será el próximo lunes.")
        compose.onNodeWithText("Curso").performTextInput("Biología")
        compose.onNodeWithText("Guardar y publicar").performScrollTo().assertIsEnabled().performClick()
        compose.waitUntil(5_000) { repository.writeCount == 1 }
        compose.onNodeWithText("Aviso de examen").performScrollTo().assertIsDisplayed()
        assertEquals(1, repository.writeCount)
    }

    @Test
    fun offlineAndEmptyStatesRemainVisibleWithoutAnEndlessLoader() {
        val repository = FakeRepository()
        repository.cache.value = CampusCache(hasServerSnapshot = true)
        repository.syncState.value = SyncState(SyncPhase.OFFLINE)
        start(repository)
        signIn()
        compose.onNodeWithText("Alertas").performClick()
        compose.onNodeWithText("Sin conexión. Tus datos guardados siguen disponibles.").assertIsDisplayed()
        compose.onNodeWithText("No hay alertas. Puedes publicar el primer aviso con Nueva alerta.").assertIsDisplayed()
        compose.onNodeWithText("Actualizando avisos…").assertDoesNotExist()
    }
}

private class FakeRepository : CampusRepository {
    val cache = MutableStateFlow(CampusCache(
        alerts = listOf(CampusAlert(
            id = "lab-42", title = "Aviso de laboratorio", message = "El laboratorio se traslada al salón 410.",
            course = "Biología", previousRoom = "Salón 105", newRoom = "Salón 410", building = "Edificio de Ciencias", authorId = "test-author"
        )),
        hasServerSnapshot = true
    ))
    override val syncState = MutableStateFlow(SyncState(SyncPhase.ONLINE))
    override val isFirebaseConfigured = false
    override val currentAuthorId = "test-author"
    var writeCount = 0
    override fun observeCache() = cache
    override suspend fun saveProfile(name: String, email: String) { cache.update { it.copy(profile = UserProfile(name, email)) } }
    override suspend fun saveAlert(alert: CampusAlert): SaveOutcome {
        writeCount++
        cache.update { it.copy(alerts = listOf(alert.copy(authorId = currentAuthorId)) + it.alerts.filterNot { old -> old.id == alert.id }) }
        return SaveOutcome(true, "Aviso publicado.")
    }
    override suspend fun refresh() = Unit
    override suspend fun synchronize(): Unit = awaitCancellation()
    override suspend fun triggerTestCrash() = Unit
}
