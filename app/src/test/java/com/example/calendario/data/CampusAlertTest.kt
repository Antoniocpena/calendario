package com.example.calendario.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CampusAlertTest {
    private fun fields() = mapOf<String, Any?>("title" to "  Classroom change  ", "message" to "  The class moved to another room.  ", "active" to true)

    @Test
    fun publishedAlertUsesEnglishFieldsAndTrimsText() {
        val alert = campusAlertFromFields("notice-1", fields(), 1234L)!!
        assertEquals("notice-1", alert.id)
        assertEquals("Classroom change", alert.title)
        assertEquals("The class moved to another room.", alert.message)
        assertEquals(1234L, alert.publishedAtMillis)
        assertFalse(alert.isClassroomChange)
    }

    @Test
    fun inactiveAndMalformedDocumentsAreIgnored() {
        assertNull(campusAlertFromFields("1", fields() + ("active" to false)))
        assertNull(campusAlertFromFields("1", fields() - "active"))
        assertNull(campusAlertFromFields("1", fields() + ("active" to "true")))
        assertNull(campusAlertFromFields("1", fields() + ("title" to "  ")))
        assertNull(campusAlertFromFields("1", fields() + ("message" to 42)))
    }

    @Test
    fun optionalMalformedFieldsDoNotCrashTheFeed() {
        val alert = campusAlertFromFields("1", fields() + mapOf("course" to 42, "building" to false))!!
        assertEquals("", alert.course)
        assertEquals("", alert.building)
    }

    @Test
    fun remoteClassroomChangesOverrideTheSharedScheduleData() {
        val alert = campusAlertFromFields(PRIMARY_ALERT_ID, fields() + mapOf("previousRoom" to "Room 302", "newRoom" to "Room 410", "building" to "Science Building"))!!
        val shared = resolveClassroomAlert(listOf(alert), true)!!
        assertEquals("Room 410", shared.newRoom)
        assertEquals("Room 302", shared.previousRoom)
        assertEquals("Science Building", shared.building)
        assertEquals(sampleClassroomAlert.course, shared.course)
        assertTrue(shared.isClassroomChange)
    }

    @Test
    fun authoritativeEmptySnapshotsDoNotRestoreSampleAlerts() {
        assertNotNull(resolveClassroomAlert(emptyList(), false))
        assertNull(resolveClassroomAlert(emptyList(), true))
        assertNull(resolveClassroomAlert(listOf(campusAlertFromFields("other-notice", fields())!!), true))
    }
    @Test
    fun editingRequiresOwnershipExceptForUnpublishedLocalDrafts() {
        val published = CampusAlert("1", "Title", "Message", authorId = "author-a")
        assertTrue(published.canBeEditedBy("author-a"))
        assertFalse(published.canBeEditedBy("author-b"))
        assertFalse(published.copy(authorId = "").canBeEditedBy(null))
        assertTrue(published.copy(authorId = "", pendingUpload = true).canBeEditedBy(null))
    }
}
