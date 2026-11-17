package com.example.calendario.ui.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calendario.ui.components.CacheNotice
import com.example.calendario.ui.components.ErrorView
import com.example.calendario.ui.state.FormularioUiState
import com.example.calendario.ui.state.UiState
import com.example.calendario.ui.theme.CampusBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    state: FormularioUiState,
    onEdit: (String, String, String) -> Unit,
    onSaveDraft: () -> Unit,
    onSend: () -> Unit,
    onRetryOperation: () -> Unit,
    onRetryDraft: () -> Unit,
    onRetryHistory: () -> Unit,
    onLoadRecords: () -> Unit,
    onBack: () -> Unit,
) {
    val registro = state.registro
    val habilitado = state.habilitado

    val isNombreValid =
        registro.nombre.isNotBlank() && registro.nombre.length <= 120
    val isCorreoValid =
        registro.correo.length <= 254 &&
            Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(registro.correo)
    val isCodigoValid = registro.codigo.length in 3..40
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
                        enabled = state.operacion !is UiState.Loading,
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
                    onEdit(it, registro.correo, registro.codigo)
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
                    onEdit(registro.nombre, it, registro.codigo)
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
                    onEdit(registro.nombre, registro.correo, it)
                },
                label = { Text("Código de estudiante") },
                enabled = habilitado,
                isError = !isCodigoValid && registro.codigo.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            )

            if (!isCodigoValid && registro.codigo.isNotEmpty()) {
                Text(
                    text = "El código debe tener entre 3 y 40 caracteres.",
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Text(
                text = "Pulsa Guardar borrador antes de cerrar " +
                    "para conservar tus cambios sin conexión.",
            )

            OutlinedButton(
                onClick = onSaveDraft,
                enabled = habilitado,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Guardar borrador")
            }

            Button(
                onClick = onSend,
                enabled = habilitado && isValid,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Enviar registro")
            }

            if (state.borrador is UiState.Loading || state.operacion is UiState.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            val borrador = state.borrador
            if (borrador is UiState.Error) {
                ErrorView(borrador.message, onRetryDraft, Modifier.fillMaxWidth())
            }
            when (val operacion = state.operacion) {
                is UiState.Error -> ErrorView(operacion.message, onRetryOperation, Modifier.fillMaxWidth())
                is UiState.Success -> Text(operacion.data)
                else -> Unit
            }

            HorizontalDivider()

            Text(
                text = "Historial local",
                style = MaterialTheme.typography.titleMedium,
            )

            when (val historial = state.historial) {
                UiState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                is UiState.Error -> ErrorView(historial.message, onRetryHistory, Modifier.fillMaxWidth())
                is UiState.Empty -> Text("Aún no hay envíos confirmados.")
                is UiState.Success -> historial.data.forEach { envio ->
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
            }

            HorizontalDivider()

            OutlinedButton(
                onClick = onLoadRecords,
                enabled = state.registros !is UiState.Loading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Consultar mis registros")
            }

            when (val registros = state.registros) {
                UiState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                is UiState.Error -> ErrorView(registros.message, onLoadRecords, Modifier.fillMaxWidth())
                is UiState.Empty -> {
                    if (registros.desdeCache) CacheNotice()
                    Text("No hay registros para mostrar. Pulsa Consultar mis registros para actualizar.")
                }
                is UiState.Success -> {
                    if (registros.desdeCache) CacheNotice()
                    registros.data.forEach { remoto ->
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
    }
}
