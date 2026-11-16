package com.example.calendario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.calendario.data.AlertFeedState
import com.example.calendario.data.AlertsRepository
import com.example.calendario.data.scheduleClassroomAlert
import com.example.calendario.data.resolveClassroomAlert
import com.example.calendario.ui.components.CampusBottomBar
import com.example.calendario.ui.components.CampusTab
import com.example.calendario.ui.screens.AlertsScreen
import com.example.calendario.ui.screens.ClassDetailScreen
import com.example.calendario.ui.screens.LoginScreen
import com.example.calendario.ui.screens.MapScreen
import com.example.calendario.ui.screens.ScheduleScreen
import com.example.calendario.ui.theme.CalendarioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendarioTheme {
                var hasCompletedLogin by rememberSaveable { mutableStateOf(false) }
                if (hasCompletedLogin) {
                    CampusContent()
                } else {
                    LoginScreen(onLoginComplete = { hasCompletedLogin = true })
                }
            }
        }
    }
}

@Composable
private fun CampusContent() {
    var selectedTab by rememberSaveable { mutableStateOf(CampusTab.SCHEDULE) }
    var showClassDetail by rememberSaveable { mutableStateOf(false) }
    var retryCount by remember { mutableStateOf(0) }
    var feedState by remember { mutableStateOf(AlertFeedState()) }
    val classroomAlert = resolveClassroomAlert(feedState.alerts, feedState.hasSnapshot)
    val currentClass = classroomAlert ?: scheduleClassroomAlert
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { AlertsRepository(context) }

    DisposableEffect(repository, retryCount) {
        val listener = repository.listen(feedState) { feedState = it }
        onDispose { listener?.remove() }
    }

    BackHandler(enabled = showClassDetail || selectedTab != CampusTab.SCHEDULE) {
        if (showClassDetail) {
            showClassDetail = false
        } else {
            selectedTab = CampusTab.SCHEDULE
        }
    }

    if (showClassDetail) {
        ClassDetailScreen(
            onBack = { showClassDetail = false },
            onOpenMap = {
                showClassDetail = false
                selectedTab = CampusTab.MAP
            },
            classroomAlert = currentClass,
            showClassroomAlert = classroomAlert != null
        )
    } else {
        Scaffold(
            bottomBar = {
                CampusBottomBar(
                    selectedTab = selectedTab,
                    onSelectTab = { selectedTab = it }
                )
            }
        ) { innerPadding ->
            val screenModifier = Modifier.padding(innerPadding)
            when (selectedTab) {
                CampusTab.SCHEDULE -> ScheduleScreen(
                    onOpenDetail = { showClassDetail = true },
                    classroomAlert = currentClass,
                    showClassroomAlert = classroomAlert != null,
                    modifier = screenModifier
                )
                CampusTab.MAP -> MapScreen(
                    classroomAlert = currentClass,
                    modifier = screenModifier
                )
                CampusTab.ALERTS -> AlertsScreen(
                    classroomAlert = classroomAlert,
                    feedState = feedState,
                    onOpenDetail = { showClassDetail = true },
                    onRetry = { retryCount++ },
                    modifier = screenModifier
                )
            }
        }
    }
}
