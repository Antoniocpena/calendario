package com.example.calendario.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.calendario.domain.Clase
import com.example.calendario.ui.theme.CampusAmber
import com.example.calendario.ui.theme.CampusAmberStrong
import com.example.calendario.ui.theme.CampusBlue
import com.example.calendario.ui.theme.CampusLightBlue
import com.example.calendario.ui.theme.CampusMuted

@Composable
fun ScheduleScreen(
    clases: List<Clase>,
    mensaje: String,
    ocupado: Boolean,
    onRefresh: () -> Unit,
    onOpenDetail: (Int) -> Unit,
    onOpenForm: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            ScheduleBottomBar()
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Schedule",
                color = CampusBlue,
                style = MaterialTheme.typography.labelLarge,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Mi horario",
                style = MaterialTheme.typography.headlineSmall,
            )

            Text(
                text = "Consulta tus clases y actualiza el horario.",
                color = CampusMuted,
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRefresh,
                enabled = !ocupado,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Actualizar desde Firestore")
            }

            if (ocupado) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (mensaje.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (clases.isEmpty() && !ocupado) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "No hay clases cargadas",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pulsa Actualizar desde Firestore con conexión.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            val claseDestacada = clases.firstOrNull()
            if (claseDestacada != null) {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle(text = "CLASE DESTACADA")
                Spacer(modifier = Modifier.height(8.dp))

                NextClassCard(
                    clase = claseDestacada,
                    onOpenDetail = {
                        onOpenDetail(claseDestacada.id)
                    },
                )
            }

            val otrasClases = clases.drop(1)
            if (otrasClases.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle(text = "MÁS CLASES")
                Spacer(modifier = Modifier.height(8.dp))

                otrasClases.forEach { clase ->
                    ScheduleItem(
                        title = clase.materia.nombre,
                        place = "${clase.salon.edificio.nombre}, salón ${clase.salon.numero}",
                        time = "${clase.dia} · ${clase.horaInicio} - ${clase.horaFin}",
                        accentColor = CampusBlue,
                        onOpenDetail = {
                            onOpenDetail(clase.id)
                        },
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onOpenForm,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Abrir formulario")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NextClassCard(
    clase: Clase,
    onOpenDetail: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = CampusAmberStrong,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "AULA ASIGNADA",
                    color = CampusAmberStrong,
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = CampusAmber,
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                ) {
                    Text(
                        text = clase.salon.edificio.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "Salón ${clase.salon.numero}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = clase.materia.nombre,
                style = MaterialTheme.typography.titleMedium,
            )

            InformationRow(
                icon = Icons.Rounded.Schedule,
                text = "${clase.dia} · ${clase.horaInicio} - ${clase.horaFin}",
            )

            InformationRow(
                icon = Icons.Rounded.LocationOn,
                text = "${clase.salon.edificio.nombre}, salón ${clase.salon.numero}",
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onOpenDetail,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
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
    accentColor: Color,
    onOpenDetail: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(42.dp)
                    .background(
                        color = accentColor,
                        shape = RoundedCornerShape(4.dp),
                    ),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = place,
                    color = CampusMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelMedium,
                )
                TextButton(onClick = onOpenDetail) {
                    Text("Ver detalle")
                }
            }
        }
    }
}

@Composable
private fun InformationRow(
    icon: ImageVector,
    text: String,
) {
    Row(
        modifier = Modifier.padding(top = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CampusBlue,
            modifier = Modifier.size(17.dp),
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = CampusBlue,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
private fun ScheduleBottomBar() {
    NavigationBar(
        containerColor = Color.White,
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = "Horario",
                )
            },
            label = {
                Text(text = "Horario")
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = CampusLightBlue,
            ),
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Map,
                    contentDescription = "Mapa",
                )
            },
            label = {
                Text(text = "Mapa")
            },
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = "Alertas",
                )
            },
            label = {
                Text(text = "Alertas")
            },
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Perfil",
                )
            },
            label = {
                Text(text = "Perfil")
            },
        )
    }
}
