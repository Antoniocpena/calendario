package com.example.calendario.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.calendario.data.CampusAlert
import com.example.calendario.data.CampusRepository
import com.example.calendario.data.canBeEditedBy
import com.example.calendario.data.PRIMARY_ALERT_ID
import com.example.calendario.data.SyncPhase
import com.example.calendario.data.SyncState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import java.text.DateFormat
import java.util.Date

class RepositoryViewModelFactory<T : ViewModel>(private val create: () -> T) : ViewModelProvider.Factory {
    override fun <V : ViewModel> create(modelClass: Class<V>): V = checkNotNull(modelClass.cast(create()))
}

class AppViewModel(repository: CampusRepository) : ViewModel() {
    val uiState: StateFlow<SyncState> = repository.syncState
    init { viewModelScope.launch { repository.synchronize() } }
}

class LoginViewModel(private val repository: CampusRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val profile = repository.observeCache().first().profile
                if (profile != null) mutableState.update {
                    it.copy(name = it.name.ifBlank { profile.name }, email = it.email.ifBlank { profile.email })
                }
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) { mutableState.update { it.copy(error = "No se pudieron recuperar tus datos guardados.") } }
        }
    }

    fun changeName(value: String) { mutableState.update { it.copy(name = value, nameError = FormValidation.name(value), error = null) } }
    fun changeEmail(value: String) { mutableState.update { it.copy(email = value, emailError = FormValidation.email(value), error = null) } }
    fun changePassword(value: String) { mutableState.update { it.copy(password = value, passwordError = FormValidation.password(value), error = null) } }

    fun submit() {
        val state = uiState.value
        if (!state.isValid || state.isSaving) return
        viewModelScope.launch {
            mutableState.update { it.copy(isSaving = true, error = null) }
            try {
                repository.saveProfile(state.name, state.email)
                mutableState.update { it.copy(isSaving = false, completed = true, password = "") }
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) { mutableState.update { it.copy(isSaving = false, error = "No se pudieron guardar tus datos. Inténtalo nuevamente.") } }
        }
    }
}

abstract class ContentViewModel(protected val repository: CampusRepository, private val destinationId: String? = null) : ViewModel() {
    val uiState: StateFlow<ContentUiState> = combine(repository.observeCache(), repository.syncState) { cache, sync ->
        ContentUiState(
            cache, sync, isCacheLoaded = true, canTestCrash = repository.isFirebaseConfigured,
            destinationId = destinationId, todayLabel = DateFormat.getDateInstance(DateFormat.FULL).format(Date())
        )
    }.catch {
        emit(ContentUiState(sync = SyncState(SyncPhase.ERROR, "No se pudieron leer los datos guardados."), isCacheLoaded = true))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ContentUiState())

    fun retry() { viewModelScope.launch { repository.refresh() } }
}

class ScheduleViewModel(repository: CampusRepository) : ContentViewModel(repository)
class MapViewModel(repository: CampusRepository, destinationId: String? = null) : ContentViewModel(repository, destinationId)
class ProfileViewModel(repository: CampusRepository) : ContentViewModel(repository)
class AlertsViewModel(repository: CampusRepository) : ContentViewModel(repository) {
    fun testCrash() { viewModelScope.launch { repository.triggerTestCrash() } }
}

class DetailViewModel(private val repository: CampusRepository, private val alertId: String) : ViewModel() {
    val uiState: StateFlow<DetailUiState> = combine(repository.observeCache(), repository.syncState) { cache, sync ->
        val alert = if (alertId == PRIMARY_ALERT_ID) cache.classroomAlert else cache.alerts.firstOrNull { it.id == alertId }
        DetailUiState(
            alert = alert,
            isLoading = false,
            canEdit = alert?.canBeEditedBy(repository.currentAuthorId) == true,
            sync = sync
        )
    }.catch {
        emit(DetailUiState(isLoading = false, sync = SyncState(SyncPhase.ERROR, "No se pudo recuperar el aviso.")))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState())

    fun retry() { viewModelScope.launch { repository.refresh() } }
}

class AlertEditorViewModel(private val repository: CampusRepository, private val alertId: String?) : ViewModel() {
    private val documentId = alertId ?: UUID.randomUUID().toString()
    private val mutableState = MutableStateFlow(AlertEditorUiState(isEditing = alertId != null))
    val uiState: StateFlow<AlertEditorUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val existing = alertId?.let { id -> repository.observeCache().first().alerts.firstOrNull { it.id == id } }
                if (alertId != null && existing == null) {
                    mutableState.update { it.copy(isLoading = false, loadError = "El aviso ya no está disponible.") }
                } else if (existing != null && !existing.canBeEditedBy(repository.currentAuthorId)) {
                    mutableState.update { it.copy(isLoading = false, loadError = "Solo puedes editar los avisos que publicaste.") }
                } else {
                    mutableState.value = validate(AlertEditorUiState(
                        title = existing?.title.orEmpty(), message = existing?.message.orEmpty(),
                        course = existing?.course.orEmpty(), previousRoom = existing?.previousRoom.orEmpty(),
                        newRoom = existing?.newRoom.orEmpty(), building = existing?.building.orEmpty(),
                        isLoading = false, isEditing = alertId != null
                    ))
                }
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) { mutableState.update { it.copy(isLoading = false, loadError = "No se pudo abrir el aviso.") } }
        }
    }

    fun change(field: AlertField, value: String) {
        mutableState.update { current ->
            val touched = current.touched + field
            val updated = when (field) {
                AlertField.TITLE -> current.copy(title = value)
                AlertField.MESSAGE -> current.copy(message = value)
                AlertField.COURSE -> current.copy(course = value)
                AlertField.PREVIOUS_ROOM -> current.copy(previousRoom = value)
                AlertField.NEW_ROOM -> current.copy(newRoom = value)
                AlertField.BUILDING -> current.copy(building = value)
            }
            validate(updated.copy(touched = touched, error = null))
        }
    }

    fun save() {
        val state = uiState.value
        if (!state.isValid || state.isSaving || state.isLoading || state.loadError != null) return
        viewModelScope.launch {
            mutableState.update { it.copy(isSaving = true, error = null) }
            try {
                val outcome = repository.saveAlert(CampusAlert(
                    id = documentId, title = state.title.trim(), message = state.message.trim(),
                    course = state.course.trim(), previousRoom = state.previousRoom.trim(),
                    newRoom = state.newRoom.trim(), building = state.building.trim()
                ))
                mutableState.update { it.copy(isSaving = false, savedMessage = outcome.message) }
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) { mutableState.update { it.copy(isSaving = false, error = "No se pudo guardar el aviso. Inténtalo nuevamente.") } }
        }
    }

    private fun validate(state: AlertEditorUiState): AlertEditorUiState {
        val errors = FormValidation.alertErrors(state)
        val roomFields = setOf(AlertField.PREVIOUS_ROOM, AlertField.NEW_ROOM, AlertField.BUILDING)
        val visibleFields = state.touched + if (state.touched.any { it in roomFields }) roomFields else emptySet()
        return state.copy(errors = errors.filterKeys { it in visibleFields }, isValid = errors.isEmpty())
    }
}
