package com.example.calendario.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calendario.ui.CalendarioViewModel
import com.example.calendario.ui.theme.CampusBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    vm: CalendarioViewModel,
    onBack: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val historial by vm.historial.collectAsStateWithLifecycle()
    val registro = state.registro
    val habilitado = state.listo && !state.ocupado

    val isNombreValid =
        registro.nombre.isNotBlank() && registro.nombre.length <= 120
    val isCorreoValid =
        registro.correo.length <= 254 &&
            Patterns.EMAIL_ADDRESS.matcher(registro.correo).matches()
    val isCodigoValid = registro.codigo.length in 5..40
    val isValid = isNombreValid && isCorreoValid && isCodigoValid &&
        runCatching { registro.validar() }.isSuccess

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Formulario", color = CampusBlue)
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = !state.ocupado,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = registro.nombre,
                onValueChange = {
                    vm.editar(
                        nombre = it,
                        correo = registro.correo,
                        codigo = registro.codigo,
                    )
                },
                label = { Text("Nombre") },
                enabled = habilitado,
                isError = !isNombreValid && registro.nombre.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            )

            if (!isNombreValid && registro.nombre.isNotEmpty()) {
                Text(
                    text = "Escribe un nombre de hasta 120 caracteres.",
                    color = MaterialTheme.colorScheme.error,
                )
            }

            OutlinedTextField(
                value = registro.correo,
                onValueChange = {
                    vm.editar(
                        nombre = registro.nombre,
                        correo = it,
                        codigo = registro.codigo,
                    )
                },
                label = { Text("Correo") },
                enabled = habilitado,
                isError = !isCorreoValid && registro.correo.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            )

            if (!isCorreoValid && registro.correo.isNotEmpty()) {
                Text(
                    text = "Escribe un correo válido de hasta 254 caracteres.",
                    color = MaterialTheme.colorScheme.error,
                )
            }

            OutlinedTextField(
                value = registro.codigo,
                onValueChange = {
                    vm.editar(
                        nombre = registro.nombre,
                        correo = registro.correo,
                        codigo = it,
                    )
                },
                label = { Text("Código de estudiante") },
                enabled = habilitado,
                isError = !isCodigoValid && registro.codigo.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            )

            if (!isCodigoValid && registro.codigo.isNotEmpty()) {
                Text(
                    text = "El código debe tener entre 5 y 40 caracteres.",
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Text(
                text = "Pulsa Guardar borrador antes de cerrar " +
                    "para conservar tus cambios sin conexión.",
            )

            OutlinedButton(
                onClick = vm::guardarBorrador,
                enabled = habilitado,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Guardar borrador en Room")
            }

            Button(
                onClick = vm::enviar,
                enabled = habilitado && isValid,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Enviar a Firebase")
            }

            if (!state.listo || state.ocupado) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (state.mensaje.isNotBlank()) {
                Text(state.mensaje)
            }

            HorizontalDivider()

            Text(
                text = "Historial local — Room",
                style = MaterialTheme.typography.titleMedium,
            )

            if (historial.isEmpty()) {
                Text("Aún no hay envíos confirmados.")
            }

            historial.forEach { envio ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
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
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Leer mis documentos de Firebase")
            }

            state.remotos.forEach { remoto ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
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
