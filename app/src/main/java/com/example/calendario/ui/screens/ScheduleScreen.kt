package com.example.calendario.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.calendario.ui.theme.CampusAmber
import com.example.calendario.ui.theme.CampusAmberStrong
import com.example.calendario.ui.theme.CampusBlue
import com.example.calendario.ui.theme.CampusGreen
import com.example.calendario.ui.theme.CampusMuted

import com.example.calendario.data.CampusAlert

@Composable
fun ScheduleScreen(
    onOpenDetail: () -> Unit,
    classroomAlert: CampusAlert,
    showClassroomAlert: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Horario", color = CampusBlue, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(24.dp))
        Text("Mi horario de hoy", style = MaterialTheme.typography.headlineSmall)
        Text("Viernes, 3 de abril", color = CampusMuted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        SectionTitle("PRÓXIMA CLASE")
        Spacer(Modifier.height(8.dp))
        NextClassCard(onOpenDetail, classroomAlert, showClassroomAlert)
        Spacer(Modifier.height(24.dp))
        SectionTitle("PRÓXIMAS")
        Spacer(Modifier.height(8.dp))
        ScheduleItem("Química Orgánica", "Edificio de Ciencias", "1:00 PM", CampusGreen)
        Spacer(Modifier.height(10.dp))
        ScheduleItem("Biología AP", "Edificio de Biología", "3:30 PM", CampusBlue)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun NextClassCard(
    onOpenDetail: () -> Unit,
    classroomAlert: CampusAlert,
    showClassroomAlert: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (showClassroomAlert) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = CampusAmberStrong,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        classroomAlert.title,
                        color = CampusAmberStrong,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(Modifier.height(8.dp))
                Surface(color = CampusAmber, shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(classroomAlert.message, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Salón original: ${classroomAlert.previousRoom}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Nuevo salón: ${classroomAlert.newRoom}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
            Text(classroomAlert.course, style = MaterialTheme.typography.titleMedium)
            InformationRow(Icons.Rounded.Schedule, "9:00 AM - 10:15 AM")
            InformationRow(Icons.Rounded.LocationOn, "${classroomAlert.building}, ${classroomAlert.newRoom}")
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onOpenDetail,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Ver detalle")
            }
        }
    }
}

@Composable
private fun ScheduleItem(
    title: String,
    place: String,
    time: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(42.dp)
                    .background(
                        color = accentColor,
                        shape = RoundedCornerShape(4.dp)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = place,
                    color = CampusMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = time,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun InformationRow(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.padding(top = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CampusBlue,
            modifier = Modifier.size(17.dp)
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = CampusBlue,
        style = MaterialTheme.typography.labelMedium
    )
}
