package com.example.calendario

import android.app.Application
import androidx.room.Room
import com.example.calendario.data.AlertsRepository
import com.example.calendario.data.CampusRepository
import com.example.calendario.data.local.CampusDatabase

class CalendarApplication : Application() {
    val repository: CampusRepository by lazy {
        val database = Room.databaseBuilder(this, CampusDatabase::class.java, "campus.db").build()
        AlertsRepository(this, database)
    }
}
