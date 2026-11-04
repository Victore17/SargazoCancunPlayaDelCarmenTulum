package victor.android.sargazocancnplayadelcarmentulum.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.BeachEntity
import victor.android.sargazocancnplayadelcarmentulum.data.local.seed.KnownBeaches

class MigrationsTest {

    @Test
    fun `migracion 1 a 2 va de la version 1 a la 2`() {
        assertEquals(1, Migrations.MIGRATION_1_2.startVersion)
        assertEquals(2, Migrations.MIGRATION_1_2.endVersion)
    }

    @Test
    fun `el borrado de playas viejas nunca toca las favoritas`() {
        val delete = Migrations.catalogStatements(KnownBeaches.all).first()

        assertTrue(delete.startsWith("DELETE FROM beaches WHERE is_favorite = 0"))
    }

    @Test
    fun `cada playa del catalogo se inserta y se actualiza`() {
        val statements = Migrations.catalogStatements(KnownBeaches.all)

        KnownBeaches.all.forEach { beach ->
            assertTrue(statements.any { it.startsWith("INSERT OR IGNORE") && "'${beach.name}'" in it })
            assertTrue(statements.any { it.startsWith("UPDATE beaches") && it.endsWith("'${beach.name}'") })
        }
    }

    @Test
    fun `comillas simples en un nombre se escapan`() {
        val statements = Migrations.catalogStatements(
            listOf(BeachEntity(name = "Playa D'Amor", latitude = 20.0, longitude = -87.0)),
        )

        assertTrue(statements.all { "D'Amor" !in it })
        assertTrue(statements.any { "'Playa D''Amor'" in it })
    }

    @Test
    fun `playa sin coordenadas usa NULL`() {
        val statements = Migrations.catalogStatements(listOf(BeachEntity(name = "Nueva")))

        assertTrue(statements.any { it.startsWith("INSERT") && "NULL, NULL" in it })
    }

    @Test
    fun `migracion 2 a 3 va de la version 2 a la 3`() {
        assertEquals(2, Migrations.MIGRATION_2_3.startVersion)
        assertEquals(3, Migrations.MIGRATION_2_3.endVersion)
    }

    @Test
    fun `migracion 2 a 3 solo actualiza coordenadas, nunca borra ni inserta`() {
        val statements = Migrations.coordinateStatements(KnownBeaches.all)

        assertEquals(KnownBeaches.all.size, statements.size)
        assertTrue(statements.all { it.startsWith("UPDATE beaches SET latitude = ") })
        assertTrue(statements.none { "DELETE" in it || "INSERT" in it })
    }

    @Test
    fun `migracion 2 a 3 no cambia favoritas ni notificaciones`() {
        val statements = Migrations.coordinateStatements(KnownBeaches.all)

        assertTrue(statements.none { "is_favorite" in it || "notifications_enabled" in it })
    }

    @Test
    fun `todas las migraciones estan registradas en orden`() {
        assertEquals(listOf(1 to 2, 2 to 3), Migrations.ALL.map { it.startVersion to it.endVersion })
    }
}
