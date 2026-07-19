package at.ee.pkmsapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface CaptureDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(CaptureEntity capture);

    @Query("SELECT * FROM captures WHERE status = :status ORDER BY createdAt ASC LIMIT :limit")
    List<CaptureEntity> capturesWithStatus(String status, int limit);

    @Query("SELECT COUNT(*) FROM captures WHERE status = :status")
    LiveData<Integer> countWithStatus(String status);

    @Query("SELECT * FROM captures ORDER BY createdAt DESC LIMIT :limit")
    LiveData<List<CaptureEntity>> latest(int limit);

    @Query("UPDATE captures SET status = :status, syncedAt = :syncedAt WHERE id = :id")
    void updateStatus(String id, String status, String syncedAt);
}
