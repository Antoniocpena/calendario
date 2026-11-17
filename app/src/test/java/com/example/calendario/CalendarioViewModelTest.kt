package com.example.calendario

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import com.example.calendario.domain.*
import com.example.calendario.ui.state.UiState
import com.example.calendario.ui.viewmodel.*
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarioViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val models = ViewModelStore()
    private val repository = FakeRepository()
    private val registro = Registro("registro-1", "Ana", "ana@example.com", "ABC123")

    @Before
    fun preparar() { Dispatchers.setMain(dispatcher) }

    @After
    fun limpiar() {
        models.clear()
        Dispatchers.resetMain()
    }

    private fun <T : ViewModel> conservar(model: T): T = model.also {
        models.put(UUID.randomUUID().toString(), it)
    }

    @Test
    fun horarioPermiteReintentarTrasUnError() = runTest {
        repository.errorClases = IllegalStateException("Sin conexión")
        val model = conservar(ScheduleViewModel(repository))
        runCurrent()
        assertEquals(UiState.Error("Sin conexión"), model.state.value)
        repository.errorClases = null
        repository.clases = Lectura(listOf(clase), desdeCache = true)
        model.actualizar()
        runCurrent()
        assertEquals(UiState.Success(listOf(clase), desdeCache = true), model.state.value)
    }

    @Test
    fun detalleUsaElIdSolicitadoYAdmiteClasesEliminadas() = runTest {
        val model = conservar(ClassDetailViewModel(repository, 42))
        runCurrent()
        assertEquals(42, repository.ultimoClassId)
        assertEquals(UiState.Empty(), model.state.value)
    }

    @Test
    fun restauraUnBorradorLocalSinExigirQueSeaValido() = runTest {
        val parcial = registro.copy(correo = "", codigo = "")
        repository.borrador = parcial
        val model = conservar(FormularioViewModel(repository, SavedStateHandle()))
        runCurrent()
        assertEquals(parcial, model.state.value.registro)
        assertTrue(model.state.value.habilitado)
        model.guardarBorrador()
        runCurrent()
        assertTrue(model.state.value.operacion is UiState.Success)
        assertEquals(parcial, repository.borrador)
    }

    @Test
    fun losCamposRestauradosTienenPrioridadSobreElBorradorAnterior() = runTest {
        repository.borrador = registro
        val model = conservar(FormularioViewModel(repository, SavedStateHandle(mapOf(
            "registro.id" to "edicion-2",
            "registro.nombre" to "Nombre editado",
            "registro.correo" to "nuevo@example.com",
            "registro.codigo" to "NUEVO",
        ))))
        runCurrent()
        assertEquals("edicion-2", model.state.value.registro.id)
        assertEquals("Nombre editado", model.state.value.registro.nombre)
    }

    @Test
    fun unErrorAlLeerElBorradorPermiteReintentarSinPerderlo() = runTest {
        repository.borrador = registro
        repository.errorBorrador = IllegalStateException("No se pudo leer el borrador")
        val model = conservar(FormularioViewModel(repository, SavedStateHandle()))
        runCurrent()
        assertFalse(model.state.value.habilitado)
        assertTrue(model.state.value.borrador is UiState.Error)
        repository.errorBorrador = null
        model.cargarBorrador()
        runCurrent()
        assertTrue(model.state.value.habilitado)
        assertEquals(registro, model.state.value.registro)
    }

    @Test
    fun bloqueaUnSegundoEnvioYConservaLosCamposHastaLaConfirmacion() = runTest {
        repository.borrador = registro
        repository.esperaEnvio = CompletableDeferred()
        val model = conservar(FormularioViewModel(repository, SavedStateHandle()))
        runCurrent()
        model.enviar()
        model.enviar()
        runCurrent()
        assertEquals(1, repository.enviados.size)
        assertFalse(model.state.value.habilitado)
        assertEquals(registro, model.state.value.registro)
        model.editar("Cambio", "cambio@example.com", "CAMBIO")
        assertEquals(registro, model.state.value.registro)
        repository.esperaEnvio!!.complete(Unit)
        runCurrent()
        assertTrue(model.state.value.operacion is UiState.Success)
        assertEquals("", model.state.value.registro.nombre)
        assertNotEquals(registro.id, model.state.value.registro.id)
        assertNull(repository.borrador)
        assertEquals(1, (model.state.value.historial as UiState.Success).data.size)
    }

    @Test
    fun unEnvioFallidoConservaElIdYSePuedeReintentar() = runTest {
        repository.borrador = registro
        repository.errorEnvio = IllegalStateException("Sin conexión")
        val model = conservar(FormularioViewModel(repository, SavedStateHandle()))
        runCurrent()
        model.enviar()
        runCurrent()
        assertTrue(model.state.value.operacion is UiState.Error)
        assertEquals(registro, model.state.value.registro)
        assertTrue(model.state.value.habilitado)
        repository.errorEnvio = null
        model.reintentarOperacion()
        runCurrent()
        assertEquals(listOf(registro.id, registro.id), repository.enviados.map { it.id })
        assertTrue(model.state.value.operacion is UiState.Success)
    }

    @Test
    fun unTimeoutDeEnvioLiberaElFormularioYConservaLosDatos() = runTest {
        repository.borrador = registro
        repository.demoraEnvio = 60_000
        val model = conservar(FormularioViewModel(repository, SavedStateHandle()))
        runCurrent()
        model.enviar()
        advanceUntilIdle()
        assertTrue(model.state.value.operacion is UiState.Error)
        assertTrue(model.state.value.habilitado)
        assertEquals(registro, model.state.value.registro)
    }

    @Test
    fun consultarRegistrosIndicaCuandoProvienenDeCache() = runTest {
        repository.registros = Lectura(listOf(registro), desdeCache = true)
        val model = conservar(FormularioViewModel(repository, SavedStateHandle()))
        runCurrent()
        model.cargarRegistros()
        runCurrent()
        assertEquals(UiState.Success(listOf(registro), desdeCache = true), model.state.value.registros)
    }

    @Test(expected = CancellationException::class)
    fun cancelarUnaConsultaNoSeConvierteEnUnErrorDePantalla() = runTest {
        consultar<List<Clase>>(estaVacio = { it.isEmpty() }) { throw CancellationException() }
    }

    private class FakeRepository : CalendarioRepository {
        override val historial = MutableStateFlow<List<EnvioConfirmado>>(emptyList())
        var borrador: Registro? = null
        var errorBorrador: Exception? = null
        var errorClases: Exception? = null
        var errorEnvio: Exception? = null
        var esperaEnvio: CompletableDeferred<Unit>? = null
        var demoraEnvio = 0L
        var clases = Lectura<List<Clase>>(emptyList())
        var registros = Lectura<List<Registro>>(emptyList())
        var ultimoClassId: Int? = null
        val enviados = mutableListOf<Registro>()

        override suspend fun leerBorrador(): Registro? {
            errorBorrador?.let { throw it }
            return borrador
        }
        override suspend fun guardarBorrador(registro: Registro) { borrador = registro }
        override suspend fun enviar(registro: Registro): String {
            enviados += registro
            guardarBorrador(registro)
            esperaEnvio?.await()
            delay(demoraEnvio)
            errorEnvio?.let { throw it }
            val ruta = "usuarios/test/registros/${registro.id}"
            historial.value = listOf(EnvioConfirmado(registro, ruta, 123L))
            borrador = null
            return ruta
        }
        override suspend fun leerRegistrosRemotos() = registros
        override suspend fun leerClases(): Lectura<List<Clase>> {
            errorClases?.let { throw it }
            return clases
        }
        override suspend fun leerClase(classId: Int): Lectura<Clase?> {
            ultimoClassId = classId
            return Lectura(clases.datos.firstOrNull { it.id == classId }, clases.desdeCache)
        }
    }
}

private val clase = Clase(
    id = 7,
    dia = "Lunes",
    horaInicio = "08:00",
    horaFin = "09:00",
    materia = Materia("MAT101", "Matemáticas", "MAT"),
    salon = Salon("101", Edificio("Edificio A", "Entrada norte")),
    catedratico = Catedratico("Docente", "docente@example.com"),
)
