package at.ee.pkms.pkmsbackend.capture;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class NoteCaptureService {

    private static final DateTimeFormatter FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);

    private final Path inboxPath;
    private final Path indexPath;

    public NoteCaptureService(NoteCaptureProperties properties) {
        this.inboxPath = properties.getVault().getInboxPath();
        this.indexPath = properties.getSync().getIndexPath();
    }

    public synchronized CaptureNoteResponse capture(CaptureNoteRequest request) {
        UUID id = parseId(request.id());
        String content = requireContent(request.content());
        Instant createdAt = parseCreatedAt(request.createdAt());
        String source = normalizeSource(request.source());

        try {
            Files.createDirectories(inboxPath);
            Path indexParent = indexPath.getParent();
            if (indexParent != null) {
                Files.createDirectories(indexParent);
            }

            Properties index = loadIndex();
            String existingFileName = index.getProperty(id.toString());
            if (existingFileName != null) {
                Path existingPath = inboxPath.resolve(existingFileName).normalize();
                return new CaptureNoteResponse(id.toString(), existingFileName, existingPath.toString(), false);
            }

            String fileName = createFileName(id, createdAt);
            Path filePath = inboxPath.resolve(fileName).normalize();
            Files.writeString(filePath, toMarkdown(id, content, createdAt, source), StandardCharsets.UTF_8);

            index.setProperty(id.toString(), fileName);
            storeIndex(index);

            return new CaptureNoteResponse(id.toString(), fileName, filePath.toString(), true);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write note capture", exception);
        }
    }

    private Properties loadIndex() throws IOException {
        Properties properties = new Properties();
        if (Files.exists(indexPath)) {
            try (InputStream inputStream = Files.newInputStream(indexPath)) {
                properties.load(inputStream);
            }
        }
        return properties;
    }

    private void storeIndex(Properties properties) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(indexPath)) {
            properties.store(outputStream, "PKMS synced capture index");
        }
    }

    private UUID parseId(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidCaptureException("Capture id is required");
        }
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            throw new InvalidCaptureException("Capture id must be a UUID");
        }
    }

    private String requireContent(String content) {
        if (content == null || content.isBlank()) {
            throw new InvalidCaptureException("Capture content is required");
        }
        return content.strip();
    }

    private Instant parseCreatedAt(String createdAt) {
        if (createdAt == null || createdAt.isBlank()) {
            throw new InvalidCaptureException("Capture createdAt is required");
        }
        try {
            return Instant.parse(createdAt);
        } catch (RuntimeException exception) {
            throw new InvalidCaptureException("Capture createdAt must be an ISO-8601 instant");
        }
    }

    private String normalizeSource(String source) {
        if (source == null || source.isBlank()) {
            return "android";
        }
        return source.strip();
    }

    private String createFileName(UUID id, Instant createdAt) {
        String suffix = id.toString().substring(0, 8);
        return FILE_TIMESTAMP.format(createdAt) + "-" + suffix + ".md";
    }

    private String toMarkdown(UUID id, String content, Instant createdAt, String source) {
        return """
                ---
                capture_id: "%s"
                source: "%s"
                created_at: "%s"
                synced_at: "%s"
                sync_status: "synced"
                ---

                %s
                """.formatted(
                id,
                escapeYaml(source),
                createdAt,
                Instant.now(),
                content
        );
    }

    private String escapeYaml(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
