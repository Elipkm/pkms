package at.ee.pkmsapp.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "captures")
public class CaptureEntity {

    @PrimaryKey
    @NonNull
    public String id;
    public String content;
    public String createdAt;
    public String source;
    public String status;
    public String syncedAt;

    public CaptureEntity(
            @NonNull String id,
            String content,
            String createdAt,
            String source,
            String status,
            String syncedAt
    ) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.source = source;
        this.status = status;
        this.syncedAt = syncedAt;
    }
}
