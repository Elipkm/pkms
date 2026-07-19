package at.ee.pkmsapp.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.ee.pkmsapp.BuildConfig
import at.ee.pkmsapp.data.AppDatabase
import at.ee.pkmsapp.data.CaptureRepository
import kotlinx.coroutines.CancellationException

class CaptureSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val repository = CaptureRepository(
            AppDatabase.getInstance(applicationContext).captureDao()
        )
        val client = NoteCaptureClient(BuildConfig.PKMS_BACKEND_BASE_URL)
        val pendingCaptures = repository.pending(limit = 20)

        if (pendingCaptures.isEmpty()) {
            return Result.success()
        }

        return try {
            pendingCaptures.forEach { capture ->
                client.upload(capture)
                repository.markSynced(capture.id)
            }
            Result.success()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.retry()
        }
    }
}
