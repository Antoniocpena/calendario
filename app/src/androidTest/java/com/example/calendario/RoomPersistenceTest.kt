package com.example.calendario

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.calendario.data.AlertsRepository
import com.example.calendario.data.local.AlertEntity
import com.example.calendario.data.local.CampusDatabase
import com.example.calendario.data.local.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomPersistenceTest {
    @Test
    fun repositoryReadsAlertsAndProfileAfterDatabaseIsClosedAndReopened() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "persistence-${UUID.randomUUID()}.db"
        var database: CampusDatabase? = null
        try {
            database = Room.databaseBuilder(context, CampusDatabase::class.java, name).build()
            database.campusDao().upsertAlert(AlertEntity().apply {
                id = "offline-alert"
                title = "Saved notice"
                message = "This notice remains available offline."
                course = "General"
                pendingUpload = true
            })
            database.campusDao().upsertProfile(UserProfileEntity().apply {
                this.name = "Alex"
                email = "alex@example.com"
            })
            database.close()
            database = Room.databaseBuilder(context, CampusDatabase::class.java, name).build()
            val snapshot = withContext(Dispatchers.Main) { AlertsRepository(context, database).observeCache().first() }
            assertEquals("Saved notice", snapshot.alerts.single().title)
            assertTrue(snapshot.alerts.single().pendingUpload)
            assertEquals("Alex", snapshot.profile?.name)
            assertEquals("alex@example.com", snapshot.profile?.email)
        } finally {
            database?.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun remoteCleanupDoesNotDeletePendingLocalWrites() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, CampusDatabase::class.java).build()
        try {
            database.campusDao().upsertAlert(AlertEntity().apply { id = "synced"; title = "Synced" })
            database.campusDao().upsertAlert(AlertEntity().apply { id = "pending"; title = "Pending"; pendingUpload = true })
            database.campusDao().deleteSyncedAlerts()
            assertEquals("pending", database.campusDao().getActiveAlerts().single().id)
        } finally { database.close() }
    }
}
