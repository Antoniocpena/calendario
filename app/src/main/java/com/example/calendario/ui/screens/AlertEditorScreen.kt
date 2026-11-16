package com.example.calendario.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calendario.ui.components.ScreenHeader
import com.example.calendario.ui.components.ValidatedField
import com.example.calendario.ui.state.AlertEditorUiState
import com.example.calendario.ui.state.AlertField

@Composable
fun AlertEditorScreen(
    state: AlertEditorUiState,
    onChange: (AlertField, String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader(if (state.isEditing) "Editar alerta" else "Nueva alerta", onBack = onBack)
        if (state.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        } else if (state.loadError != null) {
            Text(state.loadError, color = MaterialTheme.colorScheme.error)
        } else {
            ValidatedField(state.title, "Título", state.errors[AlertField.TITLE], { onChange(AlertField.TITLE, it) }, enabled = !state.isSaving)
            ValidatedField(state.message, "Mensaje", state.errors[AlertField.MESSAGE], { onChange(AlertField.MESSAGE, it) }, singleLine = false, enabled = !state.isSaving)
            ValidatedField(state.course, "Curso", state.errors[AlertField.COURSE], { onChange(AlertField.COURSE, it) }, enabled = !state.isSaving)
            Text("Cambio de aula (opcional)", style = MaterialTheme.typography.titleMedium)
            ValidatedField(state.previousRoom, "Aula anterior", state.errors[AlertField.PREVIOUS_ROOM], { onChange(AlertField.PREVIOUS_ROOM, it) }, enabled = !state.isSaving)
            ValidatedField(state.newRoom, "Aula nueva", state.errors[AlertField.NEW_ROOM], { onChange(AlertField.NEW_ROOM, it) }, enabled = !state.isSaving)
            ValidatedField(state.building, "Edificio", state.errors[AlertField.BUILDING], { onChange(AlertField.BUILDING, it) }, enabled = !state.isSaving)
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.isSaving) LinearProgressIndicator(Modifier.fillMaxWidth())
            Button(onSave, enabled = state.isValid && !state.isSaving, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.isSaving) "Guardando…" else "Guardar y publicar")
            }
        }
    }
}
