package com.example.calendario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calendario.ui.CalendarioViewModel
import com.example.calendario.ui.screens.*
import com.example.calendario.ui.theme.CalendarioTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CalendarioTheme {
                val vm: CalendarioViewModel = viewModel()

                val state by
                vm.state.collectAsStateWithLifecycle()

                var formulario by rememberSaveable {
                    mutableStateOf(false)
                }

                var claseId by rememberSaveable {
                    mutableStateOf<Int?>(null)
                }

                val claseSeleccionada =
                    state.clases.firstOrNull {
                        it.id == claseId
                    }

                when {
                    formulario -> {
                        FormularioScreen(
                            vm = vm,
                            onBack = {
                                formulario = false
                            }
                        )
                    }

                    claseSeleccionada != null -> {
                        ClassDetailScreen(
                            clase = claseSeleccionada,
                            onBack = {
                                claseId = null
                            }
                        )
                    }

                    else -> {
                        ScheduleScreen(
                            clases = state.clases,
                            mensaje = state.mensaje,
                            ocupado =
                                !state.listo || state.ocupado,
                            onRefresh = vm::cargarClases,
                            onOpenDetail = {
                                claseId = it
                            },
                            onOpenForm = {
                                formulario = true
                            }
                        )
                    }
                }
            }
        }
    }
}