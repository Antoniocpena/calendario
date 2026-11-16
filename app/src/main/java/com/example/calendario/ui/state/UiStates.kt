package com.example.calendario.ui.state

import com.example.calendario.data.CampusAlert
import com.example.calendario.data.CampusCache
import com.example.calendario.data.SyncState
import com.example.calendario.data.sampleClassroomAlert

data class LoginUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isSaving: Boolean = false,
    val completed: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean get() = FormValidation.name(name) == null &&
        FormValidation.email(email) == null && FormValidation.password(password) == null
}

data class ContentUiState(
    val cache: CampusCache = CampusCache(),
    val sync: SyncState = SyncState(),
    val isCacheLoaded: Boolean = false,
    val canTestCrash: Boolean = false,
    val destinationId: String? = null,
    val todayLabel: String = ""
) {
    val classroomAlert: CampusAlert? get() = cache.classroomAlert
    val currentClass: CampusAlert get() = cache.alerts.firstOrNull { it.id == destinationId } ?: classroomAlert ?: sampleClassroomAlert
    val visibleAlerts: List<CampusAlert> get() = listOfNotNull(classroomAlert) + cache.alerts.filterNot { it.id == sampleClassroomAlert.id }
}

data class DetailUiState(
    val alert: CampusAlert? = null,
    val isLoading: Boolean = true,
    val canEdit: Boolean = false,
    val sync: SyncState = SyncState()
)

enum class AlertField { TITLE, MESSAGE, COURSE, PREVIOUS_ROOM, NEW_ROOM, BUILDING }

data class AlertEditorUiState(
    val title: String = "",
    val message: String = "",
    val course: String = "",
    val previousRoom: String = "",
    val newRoom: String = "",
    val building: String = "",
    val touched: Set<AlertField> = emptySet(),
    val errors: Map<AlertField, String> = emptyMap(),
    val isValid: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val loadError: String? = null,
    val error: String? = null,
    val savedMessage: String? = null
)
