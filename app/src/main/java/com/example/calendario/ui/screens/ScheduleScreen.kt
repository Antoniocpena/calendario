package com.example.calendario.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calendario.domain.Clase

@Composable
fun ScheduleScreen(
    clases: List<Clase>,
    mensaje: String,
    ocupado: Boolean,
    onRefresh: () -> Unit,
    onOpenDetail: (Int) -> Unit,
    onOpenForm: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Mi horario",
                style = MaterialTheme.typography.headlineSmall
            )

            Button(
                onClick = onRefresh,
                enabled = !ocupado,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Actualizar desde Firestore")
            }

            if (ocupado) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (mensaje.isNotBlank()) {
                Text(mensaje)
            }

            if (clases.isEmpty()) {
                Text(
                    "No hay clases cargadas. " +
                            "Pulsa Actualizar con conexión."
                )
            }

            clases.forEach { clase ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = clase.materia.nombre,
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        Text(
                            "${clase.dia} · " +
                                    "${clase.horaInicio} – ${clase.horaFin}"
                        )

                        Text(
                            "${clase.salon.edificio.nombre}, " +
                                    "salón ${clase.salon.numero}"
                        )

                        TextButton(
                            onClick = {
                                onOpenDetail(clase.id)
                            }
                        ) {
                            Text("Ver detalle")
                        }
                    }
                }
            }

            Button(
                onClick = onOpenForm,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Formulario, borrador e historial")
            }
        }
    }
}