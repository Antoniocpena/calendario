package com.example.calendario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.calendario.navigation.AppDestinations
import com.example.calendario.ui.CalendarioViewModel
import com.example.calendario.ui.components.LoadingView
import com.example.calendario.ui.screens.ClassDetailScreen
import com.example.calendario.ui.screens.FormularioScreen
import com.example.calendario.ui.screens.ScheduleScreen
import com.example.calendario.ui.theme.CalendarioTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CalendarioTheme {
                val vm: CalendarioViewModel = viewModel()
                val state by vm.state.collectAsStateWithLifecycle()
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = AppDestinations.Schedule.route,
                ) {
                    composable(AppDestinations.Schedule.route) {
                        ScheduleScreen(
                            clases = state.clases,
                            mensaje = state.mensaje,
                            ocupado = !state.listo || state.ocupado,
                            onRefresh = vm::cargarClases,
                            onOpenDetail = { classId ->
                                navController.navigate(
                                    AppDestinations.Detail.createRoute(classId),
                                )
                            },
                            onOpenForm = {
                                navController.navigate(
                                    AppDestinations.Form.route,
                                )
                            },
                        )
                    }

                    composable(
                        route = AppDestinations.Detail.route,
                        arguments = listOf(
                            navArgument("classId") {
                                type = NavType.IntType
                            },
                        ),
                    ) { backStackEntry ->
                        val classId =
                            backStackEntry.arguments?.getInt("classId")
                        val claseSeleccionada =
                            state.clases.firstOrNull { it.id == classId }

                        when {
                            claseSeleccionada != null -> {
                                ClassDetailScreen(
                                    clase = claseSeleccionada,
                                    onBack = {
                                        navController.popBackStack()
                                    },
                                )
                            }

                            !state.listo || state.ocupado -> {
                                LoadingView()
                            }

                            else -> {
                                LaunchedEffect(classId) {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }

                    composable(AppDestinations.Form.route) {
                        FormularioScreen(
                            vm = vm,
                            onBack = {
                                navController.popBackStack()
                            },
                        )
                    }
                }
            }
        }
    }
}
