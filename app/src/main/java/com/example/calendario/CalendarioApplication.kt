package com.example.calendario

import android.app.Application
import android.content.Context
import com.example.calendario.data.DefaultCalendarioRepository
import com.example.calendario.data.local.CalendarioDatabase
import com.example.calendario.domain.CalendarioRepository

class CalendarioApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

class AppContainer(context: Context) {
    val repository: CalendarioRepository by lazy {
        DefaultCalendarioRepository(CalendarioDatabase.abrir(context))
    }
}
