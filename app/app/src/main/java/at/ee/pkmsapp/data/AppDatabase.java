package at.ee.pkmsapp.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CaptureEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract CaptureDao captureDao();

    public static AppDatabase getInstance(Context context) {
        if (instance != null) {
            return instance;
        }

        synchronized (AppDatabase.class) {
            if (instance == null) {
                instance = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "pkms-captures.db"
                ).build();
            }
            return instance;
        }
    }
}
