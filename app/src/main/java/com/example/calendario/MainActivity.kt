package com.example.calendario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.calendario.data.CampusRepository
import com.example.calendario.ui.components.CampusBottomBar
import com.example.calendario.ui.components.CampusTab
import com.example.calendario.ui.screens.AlertEditorScreen
import com.example.calendario.ui.screens.AlertsScreen
import com.example.calendario.ui.screens.ClassDetailScreen
import com.example.calendario.ui.screens.LoginScreen
import com.example.calendario.ui.screens.MapScreen
import com.example.calendario.ui.screens.ProfileScreen
import com.example.calendario.ui.screens.ScheduleScreen
import com.example.calendario.ui.state.AlertEditorViewModel
import com.example.calendario.ui.state.AlertsViewModel
import com.example.calendario.ui.state.AppViewModel
import com.example.calendario.ui.state.DetailViewModel
import com.example.calendario.ui.state.LoginViewModel
import com.example.calendario.ui.state.MapViewModel
import com.example.calendario.ui.state.ProfileViewModel
import com.example.calendario.ui.state.RepositoryViewModelFactory
import com.example.calendario.ui.state.ScheduleViewModel
import com.example.calendario.ui.theme.CalendarTheme
import kotlinx.coroutines.launch

private const val LOGIN_ROUTE = "login"
private const val DETAIL_ROUTE = "alert/{alertId}"
private const val EDITOR_ROUTE = "alert-editor?alertId={alertId}"
private const val MAP_ROUTE = "map?alertId={alertId}"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as CalendarApplication).repository
        setContent { CalendarTheme { CalendarApp(repository) } }
    }
}

@Composable
fun CalendarApp(repository: CampusRepository, modifier: Modifier = Modifier) {
    val controller = rememberNavController()
    val entry by controller.currentBackStackEntryAsState()
    val selectedRoute = entry?.destination?.route?.substringBefore("?")
    val snackbar = remember { SnackbarHostState() }
    val uiScope = rememberCoroutineScope()
    viewModel<AppViewModel>(factory = remember(repository) { RepositoryViewModelFactory { AppViewModel(repository) } })

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (CampusTab.entries.any { it.route == selectedRoute }) {
                CampusBottomBar(selectedRoute, onSelectTab = { tab ->
                    controller.navigate(tab.route) {
                        // Tabs open their root instead of restoring transient detail routes.
                        popUpTo(CampusTab.SCHEDULE.route) { inclusive = false }
                        launchSingleTop = true
                    }
                })
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = controller,
            startDestination = LOGIN_ROUTE,
            modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding)
        ) {
            composable(LOGIN_ROUTE) {
                val model: LoginViewModel = viewModel(factory = remember(repository) { RepositoryViewModelFactory { LoginViewModel(repository) } })
                val state by model.uiState.collectAsState()
                LaunchedEffect(state.completed) {
                    if (state.completed) controller.navigate(CampusTab.SCHEDULE.route) {
                        popUpTo(LOGIN_ROUTE) { inclusive = true }
                    }
                }
                LoginScreen(state, model::changeName, model::changeEmail, model::changePassword, model::submit)
            }
            composable(CampusTab.SCHEDULE.route) {
                val model: ScheduleViewModel = viewModel(factory = remember(repository) { RepositoryViewModelFactory { ScheduleViewModel(repository) } })
                val state by model.uiState.collectAsState()
                ScheduleScreen(state, onOpenDetail = { id -> controller.navigate("alert/${android.net.Uri.encode(id)}") }, onRetry = model::retry)
            }
            composable(CampusTab.ALERTS.route) {
                val model: AlertsViewModel = viewModel(factory = remember(repository) { RepositoryViewModelFactory { AlertsViewModel(repository) } })
                val state by model.uiState.collectAsState()
                AlertsScreen(
                    state,
                    onOpenDetail = { id -> controller.navigate("alert/${android.net.Uri.encode(id)}") },
                    onCreate = { controller.navigate("alert-editor") },
                    onRetry = model::retry,
                    onTestCrash = model::testCrash
                )
            }
            composable(MAP_ROUTE, arguments = listOf(navArgument("alertId") { type = NavType.StringType
                nullable = true
                defaultValue = null })) { destination ->
                val alertId = destination.arguments?.getString("alertId")
                val model: MapViewModel = viewModel(factory = remember(repository, alertId) { RepositoryViewModelFactory { MapViewModel(repository, alertId) } })
                val state by model.uiState.collectAsState()
                MapScreen(state, onRetry = model::retry)
            }
            composable(CampusTab.PROFILE.route) {
                val model: ProfileViewModel = viewModel(factory = remember(repository) { RepositoryViewModelFactory { ProfileViewModel(repository) } })
                val state by model.uiState.collectAsState()
                ProfileScreen(state, onLogout = {
                    controller.navigate(LOGIN_ROUTE) { popUpTo(controller.graph.id) { inclusive = true } }
                })
            }
            composable(DETAIL_ROUTE, arguments = listOf(navArgument("alertId") { type = NavType.StringType })) { destination ->
                val alertId = checkNotNull(destination.arguments?.getString("alertId"))
                val model: DetailViewModel = viewModel(factory = remember(repository, alertId) { RepositoryViewModelFactory { DetailViewModel(repository, alertId) } })
                val state by model.uiState.collectAsState()
                ClassDetailScreen(
                    state,
                    onBack = { controller.popBackStack() },
                    onOpenMap = { controller.navigate("map?alertId=${android.net.Uri.encode(alertId)}") },
                    onEdit = { controller.navigate("alert-editor?alertId=${android.net.Uri.encode(alertId)}") },
                    onRetry = model::retry
                )
            }
            composable(EDITOR_ROUTE, arguments = listOf(navArgument("alertId") { type = NavType.StringType
                nullable = true
                defaultValue = null })) { destination ->
                val alertId = destination.arguments?.getString("alertId")
                val model: AlertEditorViewModel = viewModel(factory = remember(repository, alertId) { RepositoryViewModelFactory { AlertEditorViewModel(repository, alertId) } })
                val state by model.uiState.collectAsState()
                LaunchedEffect(state.savedMessage) {
                    state.savedMessage?.let { message ->
                        controller.popBackStack()
                        uiScope.launch { snackbar.showSnackbar(message) }
                    }
                }
                AlertEditorScreen(state, model::change, model::save, onBack = { controller.popBackStack() })
            }
        }
    }
}
