package victor.android.sargazocancnplayadelcarmentulum.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.BeachEntity
import victor.android.sargazocancnplayadelcarmentulum.data.local.seed.KnownBeaches

/** Migraciones de la base de datos. */
object Migrations {

    /**
     * Versión 1 → 2: el catálogo pasa de 31 playas con nombres propios a las 100 playas con el
     * nombre oficial del semáforo. La estructura de las tablas no cambia, solo los datos:
     * - Se borran las playas viejas que ya no están en el catálogo, excepto las favoritas
     *   (las favoritas nunca se borran).
     * - Se agregan las playas nuevas del catálogo y se actualizan sus coordenadas.
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            catalogStatements(KnownBeaches.all).forEach(db::execSQL)
        }
    }

    /**
     * Versión 2 → 3: se corrigen las coordenadas de las playas (algunas quedaban en tierra o
     * lejos de la costa). Solo se actualizan las coordenadas de las playas del catálogo:
     * no se borra ni se agrega nada, así que favoritas, ajustes e historial no se tocan.
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            coordinateStatements(KnownBeaches.all).forEach(db::execSQL)
        }
    }

    val ALL = arrayOf(MIGRATION_1_2, MIGRATION_2_3)

    /** Sentencias SQL que solo actualizan las coordenadas de las playas de [catalog]. */
    fun coordinateStatements(catalog: List<BeachEntity>): List<String> = catalog.map { beach ->
        val latitude = beach.latitude?.toString() ?: "NULL"
        val longitude = beach.longitude?.toString() ?: "NULL"
        "UPDATE beaches SET latitude = $latitude, longitude = $longitude WHERE name = ${sqlString(beach.name)}"
    }

    /** Sentencias SQL que ponen el catálogo [catalog] en la tabla de playas. */
    fun catalogStatements(catalog: List<BeachEntity>): List<String> {
        val names = catalog.joinToString(", ") { sqlString(it.name) }
        return buildList {
            add("DELETE FROM beaches WHERE is_favorite = 0 AND name NOT IN ($names)")
            add("DELETE FROM level_records WHERE beach_id NOT IN (SELECT id FROM beaches)")
            catalog.forEach { beach ->
                val latitude = beach.latitude?.toString() ?: "NULL"
                val longitude = beach.longitude?.toString() ?: "NULL"
                add(
                    "INSERT OR IGNORE INTO beaches (name, latitude, longitude, is_favorite, notifications_enabled) " +
                        "VALUES (${sqlString(beach.name)}, $latitude, $longitude, 0, 1)",
                )
                add(
                    "UPDATE beaches SET latitude = $latitude, longitude = $longitude " +
                        "WHERE name = ${sqlString(beach.name)}",
                )
            }
        }
    }

    /** Texto como literal de SQL, con las comillas simples escapadas. */
    private fun sqlString(value: String): String = "'" + value.replace("'", "''") + "'"
}
