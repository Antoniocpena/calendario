package com.example.calendario.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.calendario.domain.CalendarioRepository
import com.example.calendario.ui.screens.ClassDetailScreen
import com.example.calendario.ui.screens.FormularioScreen
import com.example.calendario.ui.screens.ScheduleScreen
import com.example.calendario.ui.state.UiState
import com.example.calendario.ui.viewmodel.ClassDetailViewModel
import com.example.calendario.ui.viewmodel.FormularioViewModel
import com.example.calendario.ui.viewmodel.ScheduleViewModel

@Composable
fun CalendarioApp(repository: CalendarioRepository) {
    val navController = rememberNavController()
    val factory = remember(repository) {
        viewModelFactory {
            initializer { ScheduleViewModel(repository) }
            initializer { FormularioViewModel(repository, createSavedStateHandle()) }
            initializer {
                ClassDetailViewModel(repository, checkNotNull(createSavedStateHandle().get<Int>("classId")))
            }
        }
    }

    NavHost(navController = navController, startDestination = AppDestinations.Schedule.route) {
        composable(AppDestinations.Schedule.route) {
            val model: ScheduleViewModel = viewModel(factory = factory)
            val state by model.state.collectAsStateWithLifecycle()
            ScheduleScreen(
                state = state,
                onRefresh = model::actualizar,
                onOpenDetail = { navController.navigate(AppDestinations.Detail.createRoute(it)) },
                onOpenForm = { navController.navigate(AppDestinations.Form.route) { launchSingleTop = true } },
            )
        }
        composable(
            route = AppDestinations.Detail.route,
            arguments = listOf(navArgument("classId") { type = NavType.IntType }),
        ) {
            val model: ClassDetailViewModel = viewModel(factory = factory)
            val state by model.state.collectAsStateWithLifecycle()
            ClassDetailScreen(state = state, onRetry = model::actualizar, onBack = { navController.popBackStack() })
        }
        composable(AppDestinations.Form.route) {
            val model: FormularioViewModel = viewModel(factory = factory)
            val state by model.state.collectAsStateWithLifecycle()
            BackHandler(enabled = state.operacion is UiState.Loading) { }
            FormularioScreen(
                state = state,
                onEdit = model::editar,
                onSaveDraft = model::guardarBorrador,
                onSend = model::enviar,
                onRetryOperation = model::reintentarOperacion,
                onRetryDraft = model::cargarBorrador,
                onRetryHistory = model::cargarHistorial,
                onLoadRecords = model::cargarRegistros,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
