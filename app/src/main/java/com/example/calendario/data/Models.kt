package com.example.calendario.data

data class Edificio(
    val nombre: String,
    val ubicacion: String
)

data class Salon(
    val numero: String,
    val edificio: Edificio
)

data class Materia(
    val codigo: String,
    val nombre: String,
    val siglas: String
)

data class Catedratico(
    val nombre: String,
    val correo: String
)

data class Clase(
    val id: Int,
    val dia: String,
    val horaInicio: String,
    val horaFin: String,
    val materia: Materia,
    val salon: Salon,
    val catedratico: Catedratico
)
