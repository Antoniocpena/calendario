package com.example.calendario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.calendario.navigation.CalendarioApp
import com.example.calendario.ui.theme.CalendarioTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CalendarioTheme {
                CalendarioApp((application as CalendarioApplication).container.repository)
            }
        }
    }
}
