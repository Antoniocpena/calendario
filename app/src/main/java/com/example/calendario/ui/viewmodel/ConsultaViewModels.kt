package com.example.calendario.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendario.domain.CalendarioRepository
import com.example.calendario.domain.Clase
import com.example.calendario.domain.Lectura
import com.example.calendario.ui.state.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal fun Exception.toUiError(): UiState.Error {
    if (this is CancellationException && this !is TimeoutCancellationException) throw this
    return UiState.Error(
        if (this is TimeoutCancellationException) {
            "La operación tardó demasiado. Reintenta."
        } else {
            message?.takeIf { it.isNotBlank() } ?: "No se pudo completar la operación. Reintenta."
        }
    )
}

internal suspend fun <T> consultar(
    estaVacio: (T) -> Boolean,
    consulta: suspend () -> Lectura<T>,
): UiState<T> = try {
    val lectura = consulta()
    if (estaVacio(lectura.datos)) {
        UiState.Empty(desdeCache = lectura.desdeCache)
    } else {
        UiState.Success(lectura.datos, desdeCache = lectura.desdeCache)
    }
} catch (e: Exception) {
    e.toUiError()
}

class ScheduleViewModel(private val repository: CalendarioRepository) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<Clase>>>(UiState.Loading)
    val state = _state.asStateFlow()
    private var carga: Job? = null

    init { actualizar() }

    fun actualizar() {
        carga?.cancel()
        _state.value = UiState.Loading
        carga = viewModelScope.launch {
            _state.value = consultar(estaVacio = { it.isEmpty() }) { repository.leerClases() }
        }
    }
}

class ClassDetailViewModel(
    private val repository: CalendarioRepository,
    private val classId: Int,
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<Clase>>(UiState.Loading)
    val state = _state.asStateFlow()
    private var carga: Job? = null

    init { actualizar() }

    fun actualizar() {
        carga?.cancel()
        _state.value = UiState.Loading
        carga = viewModelScope.launch {
            _state.value = when (val resultado = consultar(estaVacio = { it == null }) {
                repository.leerClase(classId)
            }) {
                is UiState.Success -> UiState.Success(checkNotNull(resultado.data), resultado.desdeCache)
                is UiState.Empty -> resultado
                is UiState.Error -> resultado
                UiState.Loading -> UiState.Loading
            }
        }
    }
}
