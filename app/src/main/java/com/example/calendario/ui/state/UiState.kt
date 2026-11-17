package com.example.calendario.ui.state

import com.example.calendario.domain.EnvioConfirmado
import com.example.calendario.domain.Registro

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Empty(val desdeCache: Boolean = false) : UiState<Nothing>
    data class Success<T>(val data: T, val desdeCache: Boolean = false) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class FormularioUiState(
    val registro: Registro,
    val borrador: UiState<Unit> = UiState.Loading,
    val operacion: UiState<String> = UiState.Empty(),
    val historial: UiState<List<EnvioConfirmado>> = UiState.Loading,
    val registros: UiState<List<Registro>> = UiState.Empty(),
) {
    val habilitado: Boolean
        get() = borrador !is UiState.Loading && borrador !is UiState.Error &&
            operacion !is UiState.Loading
}
