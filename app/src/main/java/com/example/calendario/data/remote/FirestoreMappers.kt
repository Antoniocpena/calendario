package com.example.calendario.data.remote

import com.example.calendario.domain.*
import com.google.firebase.firestore.DocumentSnapshot

private fun DocumentSnapshot.texto(campo: String): String {
    return getString(campo)?.takeIf { it.isNotBlank() }
        ?: error("Documento $id: falta el campo $campo")
}

fun DocumentSnapshot.toRegistro(): Registro {
    return Registro(
        id = id,
        nombre = texto("nombre"),
        correo = texto("correo"),
        codigo = texto("codigo")
    ).also {
        it.validar()
    }
}

fun DocumentSnapshot.toClase(): Clase {
    return Clase(
        id = id.toIntOrNull()
            ?: error("El ID de la clase debe ser entero"),

        dia = texto("dia"),
        horaInicio = texto("horaInicio"),
        horaFin = texto("horaFin"),

        materia = Materia(
            codigo = texto("materia.codigo"),
            nombre = texto("materia.nombre"),
            siglas = texto("materia.siglas")
        ),

        salon = Salon(
            numero = texto("salon.numero"),
            edificio = Edificio(
                nombre = texto("salon.edificio.nombre"),
                ubicacion = texto("salon.edificio.ubicacion")
            )
        ),

        catedratico = Catedratico(
            nombre = texto("catedratico.nombre"),
            correo = texto("catedratico.correo")
        )
    )
}
