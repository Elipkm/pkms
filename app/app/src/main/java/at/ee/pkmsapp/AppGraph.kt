package at.ee.pkmsapp

import android.content.Context
import at.ee.pkmsapp.data.AppDatabase
import at.ee.pkmsapp.data.CaptureRepository
import at.ee.pkmsapp.sync.CaptureSyncScheduler

object AppGraph {

    fun repository(context: Context): CaptureRepository {
        return CaptureRepository(
            AppDatabase.getInstance(context).captureDao()
        )
    }

    fun syncScheduler(context: Context): CaptureSyncScheduler {
        return CaptureSyncScheduler(context)
    }
}
