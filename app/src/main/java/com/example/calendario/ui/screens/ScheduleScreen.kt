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
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import com.example.calendario.ui.theme.CampusLightBlue
import com.example.calendario.ui.theme.CampusMuted

@Composable
fun ScheduleScreen(
    onOpenDetail: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            ScheduleBottomBar()
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Schedule",
                color = CampusBlue,
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Mi horario de hoy",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Viernes, 3 de abril",
                color = CampusMuted,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(text = "PRÓXIMA CLASE")

            Spacer(modifier = Modifier.height(8.dp))

            NextClassCard(onOpenDetail = onOpenDetail)

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(text = "PRÓXIMAS")

            Spacer(modifier = Modifier.height(8.dp))

            ScheduleItem(
                title = "Química Orgánica",
                place = "Edificio de Ciencias",
                time = "1:00 PM",
                accentColor = CampusGreen
            )

            Spacer(modifier = Modifier.height(10.dp))

            ScheduleItem(
                title = "Biología AP",
                place = "Edificio de Biología",
                time = "3:30 PM",
                accentColor = CampusBlue
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NextClassCard(
    onOpenDetail: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.WarningAmber,
                    contentDescription = null,
                    tint = CampusAmberStrong,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(7.dp))

                Text(
                    text = "CAMBIO DE AULA",
                    color = CampusAmberStrong,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = CampusAmber,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Salón original: Salón 201",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Nuevo salón: Salón 302",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Cálculo II",
                style = MaterialTheme.typography.titleMedium
            )

            InformationRow(
                icon = Icons.Rounded.Schedule,
                text = "9:00 AM - 10:15 AM"
            )

            InformationRow(
                icon = Icons.Rounded.LocationOn,
                text = "Edificio de Ingeniería, salón 302"
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onOpenDetail,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Ver detalle")
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

@Composable
private fun ScheduleBottomBar() {
    NavigationBar(
        containerColor = Color.White
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = "Horario"
                )
            },
            label = {
                Text(text = "Horario")
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = CampusLightBlue
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Map,
                    contentDescription = "Mapa"
                )
            },
            label = {
                Text(text = "Mapa")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = "Alertas"
                )
            },
            label = {
                Text(text = "Alertas")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Perfil"
                )
            },
            label = {
                Text(text = "Perfil")
            }
        )
    }
}
