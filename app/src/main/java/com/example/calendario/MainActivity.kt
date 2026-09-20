package com.example.calendario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.calendario.ui.screens.ClassDetailScreen
import com.example.calendario.ui.screens.ScheduleScreen
import com.example.calendario.ui.theme.CalendarioTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CalendarioTheme {
                var currentScreen by remember {
                    mutableStateOf(AppScreen.SCHEDULE)
                }

                when (currentScreen) {
                    AppScreen.SCHEDULE -> {
                        ScheduleScreen(
                            onOpenDetail = {
                                currentScreen = AppScreen.DETAIL
                            }
                        )
                    }

                    AppScreen.DETAIL -> {
                        ClassDetailScreen(
                            onBack = {
                                currentScreen = AppScreen.SCHEDULE
                            }
                        )
                    }
                }
            }
        }
    }
}

private enum class AppScreen {
    SCHEDULE,
    DETAIL
}