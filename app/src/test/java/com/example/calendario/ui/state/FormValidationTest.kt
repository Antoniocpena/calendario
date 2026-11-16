package com.example.calendario.ui.state

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormValidationTest {
    @Test
    fun loginRequiresAllThreeValidFields() {
        assertFalse(LoginUiState().isValid)
        assertFalse(LoginUiState("Alex", "alex@example.com", "123").isValid)
        assertFalse(LoginUiState("A", "alex@example.com", "123456").isValid)
        assertFalse(LoginUiState("Alex", "invalid-email", "123456").isValid)
        assertTrue(LoginUiState("Alex", "alex@example.com", "123456").isValid)
    }

    @Test
    fun everyLoginFieldHasItsOwnValidationError() {
        assertNotNull(FormValidation.name(""))
        assertNotNull(FormValidation.email("broken@"))
        assertNotNull(FormValidation.password("      "))
        assertNull(FormValidation.email("test@example.com"))
        assertNull(FormValidation.name("Alex"))
        assertNull(FormValidation.password("secure123"))
    }

    @Test
    fun alertFormValidatesRequiredFieldsAndRoomPairs() {
        val valid = AlertEditorUiState(title = "Library notice", message = "The library will open at ten.", course = "General")
        assertTrue(FormValidation.alertErrors(valid).isEmpty())
        assertTrue(FormValidation.alertErrors(valid.copy(title = "X")).containsKey(AlertField.TITLE))
        assertTrue(FormValidation.alertErrors(valid.copy(message = "Short")).containsKey(AlertField.MESSAGE))
        assertTrue(FormValidation.alertErrors(valid.copy(course = "")).containsKey(AlertField.COURSE))
        val incomplete = valid.copy(previousRoom = "Room 105")
        assertTrue(FormValidation.alertErrors(incomplete).containsKey(AlertField.NEW_ROOM))
        val missingBuilding = incomplete.copy(newRoom = "Room 410")
        assertTrue(FormValidation.alertErrors(missingBuilding).containsKey(AlertField.BUILDING))
        assertTrue(FormValidation.alertErrors(missingBuilding.copy(building = "Science Building")).isEmpty())
    }
}
