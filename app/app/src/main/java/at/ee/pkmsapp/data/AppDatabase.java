package at.ee.pkmsapp.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {CaptureEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE captures ADD COLUMN links TEXT");
            database.execSQL("ALTER TABLE captures ADD COLUMN categories TEXT");
            database.execSQL("ALTER TABLE captures ADD COLUMN attachmentPaths TEXT");
            database.execSQL("ALTER TABLE captures ADD COLUMN attachmentNames TEXT");
            database.execSQL("ALTER TABLE captures ADD COLUMN attachmentMimeTypes TEXT");
        }
    };

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
                ).addMigrations(MIGRATION_1_2).build();
            }
            return instance;
        }
    }
}
