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
fun ClassDetailScreen(
    clase: Clase,
    onBack: () -> Unit
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
            TextButton(onClick = onBack) {
                Text("Regresar")
            }

            Text(
                text = clase.materia.nombre,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                "${clase.materia.codigo} · " +
                        clase.materia.siglas
            )

            Text(
                "${clase.dia}: " +
                        "${clase.horaInicio} – ${clase.horaFin}"
            )

            Text(
                "${clase.salon.edificio.nombre}, " +
                        "salón ${clase.salon.numero}"
            )

            Text(clase.salon.edificio.ubicacion)

            HorizontalDivider()

            Text(
                text = "Catedrático",
                style = MaterialTheme.typography.titleMedium
            )

            Text(clase.catedratico.nombre)
            Text(clase.catedratico.correo)
        }
    }
}