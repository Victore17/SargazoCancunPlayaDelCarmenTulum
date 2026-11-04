package victor.android.sargazocancnplayadelcarmentulum.data.repository

import victor.android.sargazocancnplayadelcarmentulum.data.local.dao.BeachDao
import victor.android.sargazocancnplayadelcarmentulum.data.local.dao.LevelRecordDao
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.BeachEntity
import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.LevelRecordEntity
import victor.android.sargazocancnplayadelcarmentulum.data.local.seed.KnownBeaches
import victor.android.sargazocancnplayadelcarmentulum.data.remote.SemaforoRemoteDataSource
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.domain.model.SemaforoReport
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SemaforoRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class SemaforoRepositoryImpl @Inject constructor(
    private val remote: SemaforoRemoteDataSource,
    private val beachDao: BeachDao,
    private val levelRecordDao: LevelRecordDao,
    private val clock: Clock,
) : SemaforoRepository {

    override suspend fun refresh() {
        val since = LocalDate.now(clock).minusMonths(HISTORY_MONTHS)
        saveLocally(remote.fetchSince(since))
    }

    override suspend fun publish(report: SemaforoReport) {
        remote.publish(report)
        saveLocally(listOf(report))
    }

    /**
     * Guarda los semáforos en Room. Las playas que todavía no existen se crean: con coordenadas
     * si están en el catálogo, o sin ubicación si son nuevas.
     */
    private suspend fun saveLocally(reports: List<SemaforoReport>) {
        if (reports.isEmpty()) return
        val catalog = KnownBeaches.all.associateBy { it.name }
        val existingNames = beachDao.getAll().mapTo(HashSet()) { it.name }
        val missing = reports.asSequence()
            .flatMap { it.readings }
            .map { it.beachName }
            .filter { it !in existingNames }
            .distinct()
            .map { name -> catalog[name] ?: BeachEntity(name = name) }
            .toList()
        if (missing.isNotEmpty()) beachDao.insertAll(missing)

        val idsByName = beachDao.getAll().associate { it.name to it.id }
        val records = reports.flatMap { report ->
            report.readings
                .filter { it.level != SargassumLevel.NO_DATA }
                .mapNotNull { reading ->
                    val beachId = idsByName[reading.beachName] ?: return@mapNotNull null
                    LevelRecordEntity(beachId = beachId, level = reading.level, date = report.date)
                }
        }
        levelRecordDao.upsertAll(records)
    }

    private companion object {
        /** Solo se guardan los últimos dos meses de historial. */
        const val HISTORY_MONTHS = 2L
    }
}
