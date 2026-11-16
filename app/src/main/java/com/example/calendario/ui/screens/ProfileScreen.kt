package com.example.calendario.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calendario.ui.components.ScreenHeader
import com.example.calendario.ui.state.ContentUiState

@Composable
fun ProfileScreen(state: ContentUiState, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ScreenHeader("Mi perfil")
        Text(state.cache.profile?.name ?: "Sin nombre guardado")
        Text(state.cache.profile?.email ?: "Sin correo guardado")
        Text("Tu nombre y correo quedan guardados en este dispositivo y están disponibles sin conexión.")
        OutlinedButton(onClick = onLogout) { Text("Cerrar sesión") }
    }
}
