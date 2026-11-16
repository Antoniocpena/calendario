package com.example.calendario.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal fun LazyListScope.crashlyticsTestItem(enabled: Boolean, onCrash: () -> Unit) {
    item { CrashlyticsTestCard(enabled, onCrash) }
}

@Composable
private fun CrashlyticsTestCard(enabled: Boolean, onCrash: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Prueba de Crashlytics", style = MaterialTheme.typography.titleMedium)
            Text("La app se cerrará intencionalmente. Después vuelve a abrirla con internet para enviar el reporte.")
            Button(
                onClick = onCrash,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Provocar fallo de prueba") }
        }
    }
}
