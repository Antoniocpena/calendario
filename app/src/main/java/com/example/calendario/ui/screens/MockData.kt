package com.example.calendario.ui.screens

import com.example.calendario.data.*

val mockClases = listOf(
    Clase(
        id = 1,
        dia = "Lunes",
        horaInicio = "08:00",
        horaFin = "09:30",
        materia = Materia("MAT101", "Matemáticas", "MAT"),
        salon = Salon("201", Edificio("Edificio A", "Zona 15")),
        catedratico = Catedratico("Dr. Pérez", "perez@uvg.edu.gt")
    ),
    Clase(
        id = 2,
        dia = "Martes",
        horaInicio = "10:00",
        horaFin = "11:30",
        materia = Materia("FIS102", "Física", "FIS"),
        salon = Salon("305", Edificio("Edificio B", "Zona 15")),
        catedratico = Catedratico("Ing. López", "lopez@uvg.edu.gt")
    ),
    Clase(
        id = 3,
        dia = "Viernes",
        horaInicio = "09:00",
        horaFin = "10:15",
        materia = Materia("QUI103", "Química Orgánica", "QUI"),
        salon = Salon("102", Edificio("Edificio C", "Zona 15")),
        catedratico = Catedratico("Lic. Ramírez", "ramirez@uvg.edu.gt")
    )
)
