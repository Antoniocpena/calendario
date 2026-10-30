package com.example.calendario.data.local

import android.content.Context
import androidx.room.*
import com.example.calendario.domain.Registro
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "borradores")
data class BorradorEntity(
    @PrimaryKey
    val clave: Int = 1,
    val id: String,
    val nombre: String,
    val correo: String,
    val codigo: String
) {
    fun toDomain(): Registro {
        return Registro(
            id = id,
            nombre = nombre,
            correo = correo,
            codigo = codigo
        )
    }
}

@Entity(tableName = "historial")
data class HistorialEntity(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val correo: String,
    val codigo: String,
    val rutaFirebase: String,
    val confirmadoEn: Long
)

@Dao
interface CalendarioDao {

    @Query("SELECT * FROM borradores WHERE clave = 1")
    suspend fun leerBorrador(): BorradorEntity?

    @Upsert
    suspend fun guardarBorrador(borrador: BorradorEntity)

    @Query("DELETE FROM borradores WHERE clave = 1")
    suspend fun eliminarBorrador()

    @Upsert
    suspend fun guardarHistorial(envio: HistorialEntity)

    @Query("SELECT * FROM historial ORDER BY confirmadoEn DESC")
    fun observarHistorial(): Flow<List<HistorialEntity>>
}

@Database(
    entities = [
        BorradorEntity::class,
        HistorialEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class CalendarioDatabase : RoomDatabase() {

    abstract fun dao(): CalendarioDao

    companion object {
        fun abrir(
            context: Context,
            nombre: String = "calendario.db"
        ): CalendarioDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                CalendarioDatabase::class.java,
                nombre
            ).build()
        }
    }
}