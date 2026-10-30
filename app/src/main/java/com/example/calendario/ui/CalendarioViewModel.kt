package com.example.calendario.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendario.data.DefaultCalendarioRepository
import com.example.calendario.data.local.CalendarioDatabase
import com.example.calendario.domain.*
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class CalendarioState(
    val registro: Registro = Registro(
        id = UUID.randomUUID().toString(),
        nombre = "",
        correo = "",
        codigo = ""
    ),
    val clases: List<Clase> = emptyList(),
    val remotos: List<Registro> = emptyList(),
    val listo: Boolean = false,
    val ocupado: Boolean = false,
    val mensaje: String = ""
)

class CalendarioViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = CalendarioDatabase.abrir(application)

    private val repository: CalendarioRepository =
        DefaultCalendarioRepository(db)

    private val mutable = MutableStateFlow(CalendarioState())

    val state = mutable.asStateFlow()

    val historial = repository.historial.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            try {
                repository.leerBorrador()?.let { borrador ->
                    mutable.update {
                        it.copy(registro = borrador)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutable.update {
                    it.copy(
                        mensaje = "No se pudo abrir Room: " +
                                e.localizedMessage
                    )
                }
            } finally {
                mutable.update {
                    it.copy(listo = true)
                }
            }
        }
    }

    fun editar(
        nombre: String,
        correo: String,
        codigo: String
    ) {
        if (!state.value.listo || state.value.ocupado) return

        mutable.update {
            it.copy(
                registro = it.registro.copy(
                    nombre = nombre,
                    correo = correo,
                    codigo = codigo
                )
            )
        }
    }

    private fun ejecutar(accion: suspend () -> String) {
        if (!state.value.listo || state.value.ocupado) return

        mutable.update {
            it.copy(
                ocupado = true,
                mensaje = ""
            )
        }

        viewModelScope.launch {
            try {
                val mensaje = accion()

                mutable.update {
                    it.copy(mensaje = mensaje)
                }
            } catch (e: TimeoutCancellationException) {
                mutable.update {
                    it.copy(
                        mensaje = "Sin confirmación del servidor. " +
                                "Conserva el borrador y reintenta con conexión; " +
                                "podría haberse enviado."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutable.update {
                    it.copy(
                        mensaje = "Error: ${e.localizedMessage}"
                    )
                }
            } finally {
                mutable.update {
                    it.copy(ocupado = false)
                }
            }
        }
    }

    fun guardarBorrador() = ejecutar {
        repository.guardarBorrador(state.value.registro)

        "Borrador guardado en Room. Puedes reabrir sin conexión."
    }

    fun enviar() = ejecutar {
        val ruta = repository.enviar(state.value.registro)

        mutable.update {
            it.copy(
                registro = Registro(
                    id = UUID.randomUUID().toString(),
                    nombre = "",
                    correo = "",
                    codigo = ""
                )
            )
        }

        "Confirmado por Firestore y leído desde servidor: $ruta"
    }

    fun cargarClases() = ejecutar {
        val clases = repository.leerClases()

        mutable.update {
            it.copy(clases = clases)
        }

        "${clases.size} clases leídas desde Firestore."
    }

    fun cargarRegistros() = ejecutar {
        val registros = repository.leerRegistrosRemotos()

        mutable.update {
            it.copy(remotos = registros)
        }

        "${registros.size} registros leídos desde el servidor."
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}