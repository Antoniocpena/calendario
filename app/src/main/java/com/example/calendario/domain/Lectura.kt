package com.example.calendario.domain

data class Lectura<out T>(
    val datos: T,
    val desdeCache: Boolean = false,
)
