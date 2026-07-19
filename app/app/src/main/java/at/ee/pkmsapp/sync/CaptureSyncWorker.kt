package at.ee.pkmsapp.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.ee.pkmsapp.AppGraph
import at.ee.pkmsapp.BuildConfig
import kotlinx.coroutines.CancellationException

class CaptureSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val repository = AppGraph.repository(applicationContext)
        val client = NoteCaptureClient(BuildConfig.PKMS_BACKEND_BASE_URL)
        val pendingCaptures = repository.pending(limit = 20)

        if (pendingCaptures.isEmpty()) {
            return Result.success()
        }

        return try {
            pendingCaptures.forEach { capture ->
                try {
                    client.upload(capture)
                    repository.markSynced(capture.id)
                } catch (exception: MissingLocalAttachmentException) {
                    repository.markLocalProblem(capture.id)
                }
            }
            Result.success()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.retry()
        }
    }
}
