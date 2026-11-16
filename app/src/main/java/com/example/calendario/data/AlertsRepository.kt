package com.example.calendario.data

import android.content.Context
import android.util.Log
import com.example.calendario.data.local.AlertEntity
import com.example.calendario.data.local.CacheStatusEntity
import com.example.calendario.data.local.CampusDatabase
import com.example.calendario.data.local.UserProfileEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.Callable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AlertsRepository(
    context: Context,
    private val database: CampusDatabase,
    private val network: NetworkMonitor = NetworkMonitor(context)
) : CampusRepository {
    private val appContext = context.applicationContext
    private val dao = database.campusDao()
    private val firebaseApp by lazy { FirebaseApp.initializeApp(appContext) }
    private val uploadMutex = Mutex()
    private val refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val mutableSyncState = MutableStateFlow(SyncState())
    override val syncState = mutableSyncState.asStateFlow()
    override val isFirebaseConfigured: Boolean get() = firebaseApp != null
    override val currentAuthorId: String?
        get() = firebaseApp?.let { FirebaseAuth.getInstance(it).currentUser?.uid }

    override fun observeCache(): Flow<CampusCache> = database.invalidationTracker
        .createFlow("alerts", "user_profile", "cache_status")
        .map {
        withContext(Dispatchers.IO) {
            database.runInTransaction(Callable {
                val profile = dao.getProfile()
                CampusCache(
                    alerts = dao.getActiveAlerts().map { it.toModel() },
                    profile = profile?.let { UserProfile(it.name, it.email) },
                    hasServerSnapshot = dao.getCacheStatus()?.hasServerSnapshot == true
                )
            })
        }
    }

    override suspend fun saveProfile(name: String, email: String) {
        withContext(Dispatchers.IO) {
            dao.upsertProfile(UserProfileEntity().apply {
                this.name = name.trim()
                this.email = email.trim()
            })
        }
    }

    override suspend fun saveAlert(alert: CampusAlert): SaveOutcome {
        val pending = alert.copy(
            authorId = currentAuthorId.orEmpty(),
            publishedAtMillis = System.currentTimeMillis(),
            pendingUpload = true
        )
        withContext(Dispatchers.IO) {
            val existing = dao.getAlert(alert.id)
            check(existing == null || existing.toModel().canBeEditedBy(currentAuthorId)) {
                "Only the author can edit this alert."
            }
            dao.upsertAlert(pending.toEntity())
        }
        if (!network.isOnline()) return pendingOutcome()
        return try {
            val published = withTimeoutOrNull(12_000) {
                uploadMutex.withLock { uploadPending(alert.id) }
            } == true
            if (published) SaveOutcome(true, "Aviso publicado.") else pendingOutcome()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.w(TAG, "Alert saved locally; publishing failed.", error)
            SaveOutcome(false, "Aviso guardado en este dispositivo. No se pudo publicar; puedes reintentar desde Alertas.")
        }
    }

    override suspend fun refresh() { refreshRequests.emit(Unit) }

    override suspend fun synchronize() {
        combine(network.observeOnline(), refreshRequests.onStart { emit(Unit) }) { online, _ -> online }
            .collectLatest { online ->
                if (!isFirebaseConfigured) {
                    mutableSyncState.value = SyncState(SyncPhase.NOT_CONFIGURED)
                } else if (!online) {
                    mutableSyncState.value = SyncState(SyncPhase.OFFLINE)
                } else {
                    mutableSyncState.value = SyncState(SyncPhase.LOADING)
                    coroutineScope {
                        launch {
                            try {
                                val pending = withContext(Dispatchers.IO) { dao.getPendingAlerts() }
                                for (alert in pending) {
                                    val complete = withTimeoutOrNull(12_000) {
                                        uploadMutex.withLock { uploadPending(alert.id) }
                                    }
                                    if (complete != true) {
                                        mutableSyncState.value = SyncState(SyncPhase.ERROR, "Hay avisos guardados pendientes de publicar.")
                                        break
                                    }
                                }
                            } catch (error: CancellationException) {
                                throw error
                            } catch (error: Exception) {
                                Log.w(TAG, "Pending alert synchronization failed.", error)
                                mutableSyncState.value = SyncState(SyncPhase.ERROR, "No se pudieron publicar los avisos pendientes.")
                            }
                        }
                        remoteUpdates().collect { event ->
                            when (event) {
                                is RemoteEvent.Batch -> {
                                    cacheRemote(event.alerts, authoritative = !event.fromCache)
                                    mutableSyncState.value = SyncState(
                                        if (event.fromCache) SyncPhase.CACHED else SyncPhase.ONLINE
                                    )
                                }
                                is RemoteEvent.Failure -> {
                                    Log.w(TAG, "Firestore listener failed.", event.error)
                                    mutableSyncState.value = SyncState(SyncPhase.ERROR, "No se pudieron actualizar los avisos. Se conservan los datos guardados.")
                                }
                                RemoteEvent.Slow -> {
                                    mutableSyncState.value = SyncState(SyncPhase.ERROR, "El servidor está tardando. Se conservan los datos guardados; puedes reintentar.")
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun remoteUpdates(): Flow<RemoteEvent> = callbackFlow {
        val app = firebaseApp ?: run { close(); return@callbackFlow }
        val timeout = launch { delay(10_000); trySend(RemoteEvent.Slow) }
        val registration = FirebaseFirestore.getInstance(app)
            .collection(ALERTS_COLLECTION)
            .whereEqualTo("active", true)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    timeout.cancel()
                    trySend(RemoteEvent.Failure(error))
                } else if (snapshot != null) {
                    if (!snapshot.metadata.isFromCache) timeout.cancel()
                    val alerts = snapshot.documents.mapNotNull { document ->
                        val fields = document.data ?: return@mapNotNull null
                        campusAlertFromFields(
                            document.id,
                            fields,
                            (fields["publishedAt"] as? Timestamp)?.toDate()?.time ?: 0L,
                            document.metadata.hasPendingWrites()
                        )
                    }
                    trySend(RemoteEvent.Batch(alerts, snapshot.metadata.isFromCache))
                }
            }
        awaitClose {
            timeout.cancel()
            registration.remove()
        }
    }

    private suspend fun cacheRemote(alerts: List<CampusAlert>, authoritative: Boolean) {
        withContext(Dispatchers.IO) {
            database.runInTransaction {
                for (alert in alerts) {
                    val local = dao.getAlert(alert.id)
                    if (local?.pendingUpload == true && (alert.pendingUpload || !local.sameContent(alert))) continue
                    dao.upsertAlert(alert.toEntity())
                }
                if (authoritative) {
                    if (alerts.isEmpty()) dao.deleteSyncedAlerts()
                    else dao.deleteSyncedExcept(alerts.map { it.id })
                    dao.upsertCacheStatus(CacheStatusEntity().apply { hasServerSnapshot = true })
                }
            }
        }
    }

    private suspend fun uploadPending(id: String): Boolean {
        val local = withContext(Dispatchers.IO) { dao.getAlert(id) } ?: return true
        if (!local.pendingUpload) return true
        val app = checkNotNull(firebaseApp) { "Firebase is not configured." }
        val auth = FirebaseAuth.getInstance(app)
        val user = auth.currentUser ?: checkNotNull(auth.signInAnonymously().awaitResult().user)
        check(local.authorId.isBlank() || local.authorId == user.uid) { "The alert belongs to another author." }
        local.authorId = user.uid
        withContext(Dispatchers.IO) { dao.upsertAlert(local) }
        FirebaseFirestore.getInstance(app).collection(ALERTS_COLLECTION).document(id).set(
            mapOf(
                "title" to local.title,
                "message" to local.message,
                "course" to local.course,
                "previousRoom" to local.previousRoom,
                "newRoom" to local.newRoom,
                "building" to local.building,
                "authorId" to user.uid,
                "active" to local.active,
                "publishedAt" to FieldValue.serverTimestamp()
            )
        ).awaitResult()
        withContext(Dispatchers.IO) {
            database.runInTransaction {
                val current = dao.getAlert(id)
                if (current != null && current.publishedAtMillis == local.publishedAtMillis) {
                    current.pendingUpload = false
                    dao.upsertAlert(current)
                }
            }
        }
        return true
    }

    override suspend fun triggerTestCrash() { triggerCrashlyticsTest() }

    private fun pendingOutcome() = SaveOutcome(false, "Aviso guardado en este dispositivo. Se publicará cuando haya conexión y permisos.")

    private sealed interface RemoteEvent {
        data class Batch(val alerts: List<CampusAlert>, val fromCache: Boolean) : RemoteEvent
        data class Failure(val error: Exception) : RemoteEvent
        data object Slow : RemoteEvent
    }

    private companion object { const val TAG = "AlertsRepository" }
}

private fun AlertEntity.toModel() = CampusAlert(
    id, title, message, course, previousRoom, newRoom, building, publishedAtMillis,
    authorId, active, pendingUpload
)

private fun CampusAlert.toEntity() = AlertEntity().also {
    it.id = id
    it.title = title
    it.message = message
    it.course = course
    it.previousRoom = previousRoom
    it.newRoom = newRoom
    it.building = building
    it.publishedAtMillis = publishedAtMillis
    it.authorId = authorId
    it.active = active
    it.pendingUpload = pendingUpload
}

private fun AlertEntity.sameContent(alert: CampusAlert) =
    title == alert.title && message == alert.message && course == alert.course &&
        previousRoom == alert.previousRoom && newRoom == alert.newRoom && building == alert.building &&
        authorId == alert.authorId && active == alert.active

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (continuation.isActive) {
            if (task.isSuccessful) continuation.resume(task.result)
            else continuation.resumeWithException(task.exception ?: IllegalStateException("Firebase operation failed."))
        }
    }
}
