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
    public String links;
    public String categories;
    public String attachmentPaths;
    public String attachmentNames;
    public String attachmentMimeTypes;

    public CaptureEntity(
            @NonNull String id,
            String content,
            String createdAt,
            String source,
            String status,
            String syncedAt,
            String links,
            String categories,
            String attachmentPaths,
            String attachmentNames,
            String attachmentMimeTypes
    ) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.source = source;
        this.status = status;
        this.syncedAt = syncedAt;
        this.links = links;
        this.categories = categories;
        this.attachmentPaths = attachmentPaths;
        this.attachmentNames = attachmentNames;
        this.attachmentMimeTypes = attachmentMimeTypes;
    }
}
