package at.ee.pkmsapp;

import android.content.Context;
import at.ee.pkmsapp.data.AppDatabase;
import at.ee.pkmsapp.data.CaptureRepository;
import at.ee.pkmsapp.sync.CaptureSyncScheduler;

public final class AppGraph {

    private AppGraph() {
    }

    public static CaptureRepository repository(Context context) {
        return new CaptureRepository(
                context.getApplicationContext(),
                AppDatabase.getInstance(context).captureDao()
        );
    }

    public static CaptureSyncScheduler syncScheduler(Context context) {
        return new CaptureSyncScheduler(context);
    }
}
