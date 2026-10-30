package com.example.calendario.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calendario.ui.CalendarioViewModel

@Composable
fun FormularioScreen(
    vm: CalendarioViewModel,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val historial by vm.historial.collectAsStateWithLifecycle()

    val registro = state.registro
    val habilitado = state.listo && !state.ocupado

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onBack,
                enabled = !state.ocupado
            ) {
                Text("Volver al horario")
            }

            Text(
                text = "Formulario de estudiante",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = registro.nombre,
                onValueChange = {
                    vm.editar(
                        nombre = it,
                        correo = registro.correo,
                        codigo = registro.codigo
                    )
                },
                label = { Text("Nombre") },
                enabled = habilitado,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = registro.correo,
                onValueChange = {
                    vm.editar(
                        nombre = registro.nombre,
                        correo = it,
                        codigo = registro.codigo
                    )
                },
                label = { Text("Correo") },
                enabled = habilitado,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = registro.codigo,
                onValueChange = {
                    vm.editar(
                        nombre = registro.nombre,
                        correo = registro.correo,
                        codigo = it
                    )
                },
                label = { Text("Código de estudiante") },
                enabled = habilitado,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "Pulsa Guardar borrador antes de cerrar " +
                        "para conservar tus cambios sin conexión."
            )

            OutlinedButton(
                onClick = vm::guardarBorrador,
                enabled = habilitado,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar borrador en Room")
            }

            Button(
                onClick = vm::enviar,
                enabled = habilitado &&
                        runCatching { registro.validar() }.isSuccess,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar a Firebase")
            }

            if (!state.listo || state.ocupado) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.mensaje.isNotBlank()) {
                Text(state.mensaje)
            }

            HorizontalDivider()

            Text(
                text = "Historial local — Room",
                style = MaterialTheme.typography.titleMedium
            )

            if (historial.isEmpty()) {
                Text("Aún no hay envíos confirmados.")
            }

            historial.forEach { envio ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(4.dp)
                    ) {
                        Text(envio.registro.nombre)
                        Text(envio.registro.correo)
                        Text(envio.registro.codigo)
                        Text("Documento: ${envio.rutaFirebase}")
                    }
                }
            }

            HorizontalDivider()

            OutlinedButton(
                onClick = vm::cargarRegistros,
                enabled = habilitado,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Leer mis documentos de Firebase")
            }

            state.remotos.forEach { remoto ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(4.dp)
                    ) {
                        Text("ID: ${remoto.id}")
                        Text(remoto.nombre)
                        Text(remoto.correo)
                        Text(remoto.codigo)
                    }
                }
            }
        }
    }
}