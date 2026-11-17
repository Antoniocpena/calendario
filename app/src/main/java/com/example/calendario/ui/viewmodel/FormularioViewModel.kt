package com.example.calendario.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendario.domain.CalendarioRepository
import com.example.calendario.domain.Registro
import com.example.calendario.ui.state.FormularioUiState
import com.example.calendario.ui.state.UiState
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class FormularioViewModel(
    private val repository: CalendarioRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val restaurado = savedStateHandle.contains("registro.id")
    private val _state = MutableStateFlow(
        FormularioUiState(
            registro = Registro(
                id = savedStateHandle["registro.id"] ?: UUID.randomUUID().toString(),
                nombre = savedStateHandle["registro.nombre"] ?: "",
                correo = savedStateHandle["registro.correo"] ?: "",
                codigo = savedStateHandle["registro.codigo"] ?: "",
            )
        )
    )
    val state = _state.asStateFlow()
    private var borradorJob: Job? = null
    private var historialJob: Job? = null
    private var ultimaOperacion: Operacion? = null

    private enum class Operacion { GUARDAR, ENVIAR }

    init {
        cargarBorrador()
        cargarHistorial()
    }

    private fun recordar(registro: Registro) {
        savedStateHandle["registro.id"] = registro.id
        savedStateHandle["registro.nombre"] = registro.nombre
        savedStateHandle["registro.correo"] = registro.correo
        savedStateHandle["registro.codigo"] = registro.codigo
    }

    fun editar(nombre: String, correo: String, codigo: String) {
        if (!_state.value.habilitado) return
        val registro = _state.value.registro.copy(nombre = nombre, correo = correo, codigo = codigo)
        recordar(registro)
        _state.update { it.copy(registro = registro, operacion = UiState.Empty()) }
    }

    fun cargarBorrador() {
        if (_state.value.borrador is UiState.Loading && borradorJob?.isActive == true) return
        _state.update { it.copy(borrador = UiState.Loading) }
        borradorJob = viewModelScope.launch {
            try {
                val borrador = withTimeout(5_000) { repository.leerBorrador() }
                val registro = if (restaurado) _state.value.registro else borrador ?: _state.value.registro
                recordar(registro)
                _state.update {
                    it.copy(registro = registro, borrador = if (borrador == null) UiState.Empty() else UiState.Success(Unit))
                }
            } catch (e: Exception) {
                val error = e.toUiError()
                _state.update { it.copy(borrador = error) }
            }
        }
    }

    fun cargarHistorial() {
        historialJob?.cancel()
        _state.update { it.copy(historial = UiState.Loading) }
        historialJob = viewModelScope.launch {
            try {
                repository.historial.collect { envios ->
                    _state.update {
                        it.copy(historial = if (envios.isEmpty()) UiState.Empty() else UiState.Success(envios))
                    }
                }
            } catch (e: Exception) {
                val error = e.toUiError()
                _state.update { it.copy(historial = error) }
            }
        }
    }

    fun guardarBorrador() = ejecutar(Operacion.GUARDAR)

    fun enviar() = ejecutar(Operacion.ENVIAR)

    fun reintentarOperacion() {
        ultimaOperacion?.let(::ejecutar)
    }

    private fun ejecutar(operacion: Operacion) {
        if (!_state.value.habilitado) return
        val registro = _state.value.registro
        ultimaOperacion = operacion
        _state.update { it.copy(operacion = UiState.Loading) }
        viewModelScope.launch {
            try {
                when (operacion) {
                    Operacion.GUARDAR -> {
                        withTimeout(5_000) { repository.guardarBorrador(registro) }
                        _state.update {
                            it.copy(borrador = UiState.Success(Unit), operacion = UiState.Success("Borrador guardado en este dispositivo."))
                        }
                    }
                    Operacion.ENVIAR -> {
                        registro.validar()
                        withTimeout(30_000) { repository.enviar(registro) }
                        val nuevo = Registro(UUID.randomUUID().toString(), "", "", "")
                        recordar(nuevo)
                        _state.update {
                            it.copy(
                                registro = nuevo,
                                borrador = UiState.Empty(),
                                operacion = UiState.Success("Registro enviado y confirmado en el servidor."),
                                registros = UiState.Empty(),
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                val error = e.toUiError()
                _state.update { it.copy(operacion = error) }
            }
        }
    }

    fun cargarRegistros() {
        if (_state.value.registros is UiState.Loading) return
        _state.update { it.copy(registros = UiState.Loading) }
        viewModelScope.launch {
            val registros = consultar(estaVacio = { it.isEmpty() }) { repository.leerRegistrosRemotos() }
            _state.update { it.copy(registros = registros) }
        }
    }
}
