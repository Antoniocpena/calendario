package com.example.calendario.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.calendario.data.CampusAlert
import com.example.calendario.ui.components.AlertCard
import com.example.calendario.ui.components.CampusIcon
import com.example.calendario.ui.components.CampusSymbol
import com.example.calendario.ui.components.SyncBanner
import com.example.calendario.ui.state.ContentUiState
import com.example.calendario.ui.theme.CampusBlue
import com.example.calendario.ui.theme.CampusBlueDark
import com.example.calendario.ui.theme.CampusGreen
import com.example.calendario.ui.theme.CampusLightBlue
import com.example.calendario.ui.theme.CampusMuted
import com.example.calendario.ui.theme.CampusOutline

@Composable
fun ScheduleScreen(
    state: ContentUiState,
    onOpenDetail: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ScheduleHeader(state.todayLabel)
        NextClassCard(state.currentClass)
        SyncBanner(state.sync, onRetry)
        state.classroomAlert?.let { AlertCard(it, onOpenDetail = { onOpenDetail(it.id) }) }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Más tarde",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "2 clases",
                modifier = Modifier.background(CampusLightBlue, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = CampusBlue,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ScheduleItem("Química Orgánica", "Edificio de Ciencias", "1:00", "PM", CampusGreen)
            ScheduleItem("Biología AP", "Edificio de Biología", "3:30", "PM", CampusBlue)
        }
    }
}

@Composable
private fun ScheduleHeader(dateLabel: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Mi horario de hoy", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CampusIcon(CampusSymbol.CALENDAR, null, Modifier.size(18.dp), tint = CampusBlue)
            Text(dateLabel, color = CampusMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun NextClassCard(alert: CampusAlert, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CampusBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(CampusBlue, CampusBlueDark)))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("PRÓXIMA CLASE", color = Color.White, style = MaterialTheme.typography.labelMedium)
            Text(
                alert.course,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "9:00 AM – 10:15 AM",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CampusIcon(CampusSymbol.PIN, null, Modifier.size(18.dp), tint = Color.White)
                Text(
                    "${alert.building}, ${alert.newRoom}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ScheduleItem(
    title: String,
    location: String,
    time: String,
    period: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CampusOutline)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.width(66.dp).height(68.dp)
                    .background(accent.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(time, color = accent, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(period, color = CampusMuted, style = MaterialTheme.typography.labelMedium)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(location, color = CampusMuted, style = MaterialTheme.typography.bodySmall)
            }
            Box(Modifier.width(3.dp).height(32.dp).background(accent, RoundedCornerShape(2.dp)))
        }
    }
}
