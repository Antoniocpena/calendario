package com.example.calendario.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.calendario.R
import com.example.calendario.data.CampusAlert
import com.example.calendario.data.SyncPhase
import com.example.calendario.data.SyncState
import com.example.calendario.ui.theme.CampusAmber
import com.example.calendario.ui.theme.CampusBlue
import com.example.calendario.ui.theme.CampusMuted
import java.text.DateFormat
import java.util.Date

enum class CampusSymbol(val resource: Int) {
    CALENDAR(R.drawable.ic_calendar), MAP(R.drawable.ic_map), ALERTS(R.drawable.ic_alerts),
    PROFILE(R.drawable.ic_profile), BACK(R.drawable.ic_back), PIN(R.drawable.ic_pin), WARNING(R.drawable.ic_warning)
}

@Composable
fun CampusIcon(
    symbol: CampusSymbol,
    description: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    Icon(painterResource(symbol.resource), description, modifier, tint)
}

@Composable
fun ScreenHeader(title: String, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconButton(onClick = onBack) { CampusIcon(CampusSymbol.BACK, "Regresar") }
        }
        Text(title, style = MaterialTheme.typography.headlineSmall, color = CampusBlue)
    }
}

@Composable
fun SyncBanner(state: SyncState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    if (state.phase == SyncPhase.ONLINE) return
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.phase == SyncPhase.LOADING) LinearProgressIndicator(Modifier.fillMaxWidth())
            Text(
                text = when (state.phase) {
                    SyncPhase.LOADING -> "Actualizando avisos…"
                    SyncPhase.CACHED -> "Mostrando los últimos avisos guardados."
                    SyncPhase.OFFLINE -> "Sin conexión. Tus datos guardados siguen disponibles."
                    SyncPhase.ERROR -> state.message ?: "No se pudieron actualizar los avisos."
                    SyncPhase.NOT_CONFIGURED -> "Los avisos en línea todavía no están disponibles."
                    SyncPhase.ONLINE -> ""
                },
                color = if (state.phase == SyncPhase.ERROR) MaterialTheme.colorScheme.error else CampusMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            if (state.phase in listOf(SyncPhase.ERROR, SyncPhase.OFFLINE, SyncPhase.CACHED)) {
                TextButton(onClick = onRetry) { Text("Reintentar") }
            }
        }
    }
}

@Composable
fun AlertBody(alert: CampusAlert, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (alert.course.isNotBlank()) Text(alert.course, fontWeight = FontWeight.SemiBold)
        Text(alert.message)
        if (alert.isClassroomChange) {
            Text("Aula anterior: ${alert.previousRoom}")
            Text("Nuevo salón: ${alert.newRoom}", fontWeight = FontWeight.Bold)
        }
        if (alert.building.isNotBlank()) Text(alert.building)
        if (alert.pendingUpload) {
            Text("Pendiente de publicar · guardado en este dispositivo", color = CampusBlue)
        } else if (alert.publishedAtMillis > 0) {
            Text(
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(alert.publishedAtMillis)),
                color = CampusMuted, style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun AlertCard(alert: CampusAlert, onOpenDetail: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.isClassroomChange) CampusAmber else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CampusIcon(if (alert.isClassroomChange) CampusSymbol.WARNING else CampusSymbol.ALERTS, null, Modifier.size(22.dp))
                Text(alert.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            }
            AlertBody(alert)
            TextButton(onClick = onOpenDetail) { Text("Ver detalle") }
        }
    }
}

@Composable
fun ValidatedField(
    value: String,
    label: String,
    error: String?,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        enabled = enabled,
        modifier = modifier.fillMaxWidth()
    )
}
