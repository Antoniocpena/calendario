package com.example.calendario.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calendario.ui.components.AlertBody
import com.example.calendario.ui.components.ScreenHeader
import com.example.calendario.ui.components.SyncBanner
import com.example.calendario.ui.state.DetailUiState

@Composable
fun ClassDetailScreen(
    state: DetailUiState,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onEdit: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader("Detalle de la alerta", onBack = onBack)
        SyncBanner(state.sync, onRetry)
        if (state.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        } else if (state.alert == null) {
            Text("Este aviso ya no está disponible.")
            OutlinedButton(onClick = onRetry) { Text("Reintentar") }
        } else {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(state.alert.title, style = MaterialTheme.typography.titleLarge)
                    AlertBody(state.alert)
                }
            }
            if (state.alert.building.isNotBlank()) {
                Button(onClick = onOpenMap, modifier = Modifier.fillMaxWidth()) { Text("Ver cómo llegar") }
            }
            if (state.canEdit) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) { Text("Editar alerta") }
            }
        }
    }
}
