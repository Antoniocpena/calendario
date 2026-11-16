package com.example.calendario.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calendario.ui.components.AlertCard
import com.example.calendario.ui.components.ScreenHeader
import com.example.calendario.ui.components.SyncBanner
import com.example.calendario.ui.components.crashlyticsTestItem
import com.example.calendario.ui.state.ContentUiState

@Composable
fun AlertsScreen(
    state: ContentUiState,
    onOpenDetail: (String) -> Unit,
    onCreate: () -> Unit,
    onRetry: () -> Unit,
    onTestCrash: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenHeader("Tus alertas") }
        item { Button(onClick = onCreate) { Text("Nueva alerta") } }
        item { SyncBanner(state.sync, onRetry) }
        if (!state.isCacheLoaded) {
            item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        } else if (state.visibleAlerts.isEmpty()) {
            item { Text("No hay alertas. Puedes publicar el primer aviso con Nueva alerta.") }
        }
        items(state.visibleAlerts, key = { it.id }) { alert ->
            AlertCard(alert, onOpenDetail = { onOpenDetail(alert.id) })
        }
        crashlyticsTestItem(state.canTestCrash, onTestCrash)
    }
}
