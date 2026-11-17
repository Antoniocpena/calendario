package com.example.calendario.navigation

sealed class AppDestinations(
    val route: String,
) {

    data object Schedule :
        AppDestinations("schedule")

    data object Form :
        AppDestinations("form")

    data object Detail :
        AppDestinations("detail/{classId}") {

        fun createRoute(
            classId: Int,
        ): String {
            return "detail/$classId"
        }
    }
}
