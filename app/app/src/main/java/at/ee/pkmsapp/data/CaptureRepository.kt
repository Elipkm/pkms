package at.ee.pkmsapp.data

import androidx.lifecycle.LiveData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CaptureRepository(
    private val dao: CaptureDao,
) {

    val pendingCount: LiveData<Int> = dao.countWithStatus(CaptureStatus.Pending)
    val syncedCount: LiveData<Int> = dao.countWithStatus(CaptureStatus.Synced)
    val latestCaptures: LiveData<List<CaptureEntity>> = dao.latest(10)

    suspend fun saveNote(content: String): Boolean {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) {
            return false
        }

        withContext(Dispatchers.IO) {
            dao.insert(
                CaptureEntity(
                    UUID.randomUUID().toString(),
                    trimmed,
                    nowIsoInstant(),
                    "android",
                    CaptureStatus.Pending,
                    null,
                )
            )
        }
        return true
    }

    suspend fun pending(limit: Int): List<CaptureEntity> = withContext(Dispatchers.IO) {
        dao.capturesWithStatus(CaptureStatus.Pending, limit)
    }

    suspend fun markSynced(id: String) = withContext(Dispatchers.IO) {
        dao.updateStatus(id, CaptureStatus.Synced, nowIsoInstant())
    }
}

fun nowIsoInstant(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date())
}
