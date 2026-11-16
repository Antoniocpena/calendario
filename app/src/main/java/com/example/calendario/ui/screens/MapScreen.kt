package com.example.calendario.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.calendario.data.CampusAlert
import com.example.calendario.ui.state.ContentUiState
import com.example.calendario.ui.components.CampusIcon
import com.example.calendario.ui.components.CampusSymbol
import com.example.calendario.ui.components.SyncBanner
import com.example.calendario.ui.theme.CampusBlue
import com.example.calendario.ui.theme.CampusGreen
import com.example.calendario.ui.theme.CampusLightBlue
import com.example.calendario.ui.theme.CampusMuted
import com.example.calendario.ui.theme.CampusSurface

@Composable
fun MapScreen(
    state: ContentUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val classroomAlert = state.currentClass
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Mapa del campus",
            color = CampusBlue,
            style = MaterialTheme.typography.headlineSmall
        )
        Text("Mapa ilustrativo · Recorrido de ejemplo", color = CampusMuted)
        SyncBanner(state.sync, onRetry)
        CampusMapMockup(classroomAlert)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(10.dp).background(CampusBlue, CircleShape))
            Text("Entrada", modifier = Modifier.weight(1f))
            CampusIcon(CampusSymbol.PIN, null, tint = CampusBlue)
            Text("Destino")
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CampusSurface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("TU PRÓXIMA CLASE", color = CampusBlue, style = MaterialTheme.typography.labelMedium)
                Text(classroomAlert.course, style = MaterialTheme.typography.titleLarge)
                Text(classroomAlert.building, fontWeight = FontWeight.SemiBold)
                Text(classroomAlert.newRoom)
                Text("Entra al campus y sigue el recorrido marcado hasta tu destino.")
            }
        }
        Text(
            "Esta maqueta muestra ubicaciones de ejemplo; no utiliza tu ubicación ni navegación GPS.",
            color = CampusMuted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun CampusMapMockup(destination: CampusAlert, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEAF2E8))
            .semantics(mergeDescendants = true) {
                contentDescription = "Mapa ilustrativo del campus: entrada al sur, biblioteca al noroeste, " +
                    "Ciencias al suroeste, Biología al sureste y el destino al noreste. " +
                    "La ruta azul une la entrada con ${destination.building}, ${destination.newRoom}."
            }
    ) {
        val mapWidth = maxWidth
        val mapHeight = maxHeight
        Canvas(Modifier.fillMaxSize()) {
            drawRoundRect(
                color = Color(0xFFD6E7CF),
                topLeft = Offset(size.width * 0.39f, size.height * 0.07f),
                size = Size(size.width * 0.15f, size.height * 0.3f),
                cornerRadius = CornerRadius(16.dp.toPx())
            )
            drawLine(
                color = Color.White,
                start = Offset(0f, size.height * 0.49f),
                end = Offset(size.width, size.height * 0.49f),
                strokeWidth = 26.dp.toPx()
            )
            drawLine(
                color = Color.White,
                start = Offset(size.width * 0.48f, size.height * 0.38f),
                end = Offset(size.width * 0.48f, size.height),
                strokeWidth = 24.dp.toPx()
            )
            val route = Path().apply {
                moveTo(size.width * 0.18f, size.height * 0.88f)
                lineTo(size.width * 0.48f, size.height * 0.88f)
                lineTo(size.width * 0.48f, size.height * 0.49f)
                lineTo(size.width * 0.76f, size.height * 0.49f)
                lineTo(size.width * 0.76f, size.height * 0.35f)
            }
            drawPath(
                path = route,
                color = CampusBlue,
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(9.dp.toPx(), 6.dp.toPx()))
                )
            )
            drawCircle(
                color = CampusBlue,
                radius = 7.dp.toPx(),
                center = Offset(size.width * 0.18f, size.height * 0.88f)
            )
            drawCircle(
                color = CampusBlue,
                radius = 7.dp.toPx(),
                center = Offset(size.width * 0.76f, size.height * 0.35f)
            )
        }
        MapBuilding(
            "Biblioteca",
            Modifier.offset(mapWidth * 0.06f, mapHeight * 0.1f)
                .width(mapWidth * 0.3f).height(mapHeight * 0.23f)
        )
        MapBuilding(
            destination.building.removePrefix("Edificio de ") + "\n" + destination.newRoom,
            Modifier.offset(mapWidth * 0.59f, mapHeight * 0.1f)
                .width(mapWidth * 0.34f).height(mapHeight * 0.23f),
            selected = true
        )
        MapBuilding(
            "Ciencias",
            Modifier.offset(mapWidth * 0.06f, mapHeight * 0.6f)
                .width(mapWidth * 0.3f).height(mapHeight * 0.19f)
        )
        MapBuilding(
            "Biología",
            Modifier.offset(mapWidth * 0.6f, mapHeight * 0.6f)
                .width(mapWidth * 0.33f).height(mapHeight * 0.19f)
        )
        Text(
            "Entrada",
            modifier = Modifier.offset(mapWidth * 0.06f, mapHeight * 0.92f),
            color = CampusBlue,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            "Jardín",
            modifier = Modifier.offset(mapWidth * 0.38f, mapHeight * 0.39f),
            color = CampusGreen,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun MapBuilding(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) CampusLightBlue else CampusSurface)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) CampusBlue else CampusMuted,
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
