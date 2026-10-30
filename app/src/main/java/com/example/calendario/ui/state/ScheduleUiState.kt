package com.example.calendario.ui.state

import com.example.calendario.domain.Clase

sealed interface ScheduleUiState {

    data object Loading :
        ScheduleUiState

    data object Empty :
        ScheduleUiState

    data class Success(
        val classes: List<Clase>
    ) : ScheduleUiState

    data class Error(
        val message: String
    ) : ScheduleUiState
}
