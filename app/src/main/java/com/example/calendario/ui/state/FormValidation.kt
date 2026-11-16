package com.example.calendario.ui.state

object FormValidation {
    fun name(value: String): String? = when {
        value.isBlank() -> "Ingresa tu nombre."
        value.trim().length < 2 -> "Usa al menos 2 caracteres."
        else -> null
    }

    fun email(value: String): String? = when {
        value.isBlank() -> "Ingresa tu correo electrónico."
        !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(value.trim()) -> "Ingresa un correo electrónico válido."
        else -> null
    }

    fun password(value: String): String? = when {
        value.isBlank() -> "Ingresa una contraseña."
        value.length < 6 -> "Usa al menos 6 caracteres."
        else -> null
    }

    fun alertErrors(state: AlertEditorUiState): Map<AlertField, String> = buildMap {
        if (state.title.trim().length !in 3..100) put(AlertField.TITLE, "Usa entre 3 y 100 caracteres.")
        if (state.message.trim().length !in 10..1000) put(AlertField.MESSAGE, "Usa entre 10 y 1000 caracteres.")
        if (state.course.trim().length !in 2..100) put(AlertField.COURSE, "Usa entre 2 y 100 caracteres.")
        if (state.previousRoom.isNotBlank() && state.newRoom.isBlank()) put(AlertField.NEW_ROOM, "Indica el nuevo salón.")
        if (state.newRoom.isNotBlank() && state.previousRoom.isBlank()) put(AlertField.PREVIOUS_ROOM, "Indica el salón anterior.")
        if (state.newRoom.isNotBlank() && state.building.isBlank()) put(AlertField.BUILDING, "Indica el edificio.")
        if (state.previousRoom.length > 100) put(AlertField.PREVIOUS_ROOM, "Máximo 100 caracteres.")
        if (state.newRoom.length > 100) put(AlertField.NEW_ROOM, "Máximo 100 caracteres.")
        if (state.building.length > 100) put(AlertField.BUILDING, "Máximo 100 caracteres.")
    }
}
