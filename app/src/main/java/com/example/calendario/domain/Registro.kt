package com.example.calendario.domain

data class Registro(
    val id: String,
    val nombre: String,
    val correo: String,
    val codigo: String
) {
    fun validar() {
        require(
            nombre.isNotBlank() && nombre.length <= 120
        ) {
            "Escribe un nombre de hasta 120 caracteres."
        }

        require(
            correo.length <= 254 &&
                    Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                        .matches(correo)
        ) {
            "Escribe un correo válido."
        }

        require(codigo.length in 3..40) {
            "El código debe tener entre 3 y 40 caracteres."
        }
    }
}