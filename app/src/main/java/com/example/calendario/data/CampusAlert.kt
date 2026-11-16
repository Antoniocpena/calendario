package com.example.calendario.data

const val PRIMARY_ALERT_ID = "classroom-change-calculus-ii"
const val ALERTS_COLLECTION = "alerts"

data class CampusAlert(
    val id: String,
    val title: String,
    val message: String,
    val course: String = "",
    val previousRoom: String = "",
    val newRoom: String = "",
    val building: String = "",
    val publishedAtMillis: Long = 0L,
    val authorId: String = "",
    val active: Boolean = true,
    val pendingUpload: Boolean = false
) {
    val isClassroomChange: Boolean
        get() = previousRoom.isNotBlank() && newRoom.isNotBlank()
}

val sampleClassroomAlert = CampusAlert(
    id = PRIMARY_ALERT_ID,
    title = "Cambio de aula",
    message = "Esta clase cambió de salón. Dirígete a la nueva ubicación indicada abajo.",
    course = "Cálculo II",
    previousRoom = "Salón 105",
    newRoom = "Salón 302",
    building = "Edificio de Ingeniería"
)

data class UserProfile(val name: String, val email: String)

data class CampusCache(
    val alerts: List<CampusAlert> = emptyList(),
    val profile: UserProfile? = null,
    val hasServerSnapshot: Boolean = false
) {
    val classroomAlert: CampusAlert?
        get() = resolveClassroomAlert(alerts, hasServerSnapshot)
}

enum class SyncPhase { LOADING, ONLINE, CACHED, OFFLINE, ERROR, NOT_CONFIGURED }

data class SyncState(val phase: SyncPhase = SyncPhase.LOADING, val message: String? = null)

data class SaveOutcome(val published: Boolean, val message: String)

fun campusAlertFromFields(
    id: String,
    fields: Map<String, Any?>,
    publishedAtMillis: Long = 0L,
    pendingUpload: Boolean = false
): CampusAlert? {
    if (fields["active"] != true) return null
    fun text(key: String) = (fields[key] as? String)?.trim().orEmpty()
    val title = text("title")
    val message = text("message")
    if (title.isBlank() || message.isBlank()) return null
    return CampusAlert(
        id, title, message, text("course"), text("previousRoom"), text("newRoom"),
        text("building"), publishedAtMillis, text("authorId"), pendingUpload = pendingUpload
    )
}

fun resolveClassroomAlert(alerts: List<CampusAlert>, hasSnapshot: Boolean): CampusAlert? {
    val remote = alerts.firstOrNull { it.id == PRIMARY_ALERT_ID }
        ?: return sampleClassroomAlert.takeUnless { hasSnapshot }
    return remote.copy(
        course = remote.course.ifBlank { sampleClassroomAlert.course },
        previousRoom = remote.previousRoom.ifBlank { sampleClassroomAlert.previousRoom },
        newRoom = remote.newRoom.ifBlank { sampleClassroomAlert.newRoom },
        building = remote.building.ifBlank { sampleClassroomAlert.building }
    )
}

fun CampusAlert.canBeEditedBy(userId: String?): Boolean =
    (pendingUpload && authorId.isBlank()) || (authorId.isNotBlank() && authorId == userId)
