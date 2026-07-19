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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class NoteCaptureService {

    private static final DateTimeFormatter FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);
    private static final Pattern SAFE_FILE_NAME = Pattern.compile("[^A-Za-z0-9._-]");

    private final Path inboxPath;
    private final Path indexPath;
    private final String attachmentDirectory;

    public NoteCaptureService(NoteCaptureProperties properties) {
        this.inboxPath = properties.getVault().getInboxPath();
        this.indexPath = properties.getSync().getIndexPath();
        this.attachmentDirectory = normalizeAttachmentDirectory(properties.getVault().getAttachmentDirectory());
    }

    public synchronized CaptureNoteResponse capture(CaptureNoteRequest request) {
        UUID id = parseId(request.id());
        String content = requireContent(request.content());
        Instant createdAt = parseCreatedAt(request.createdAt());

        try {
            prepareStorage();

            Properties index = loadIndex();
            String existingFileName = index.getProperty(id.toString());
            if (existingFileName != null) {
                Path existingPath = inboxPath.resolve(existingFileName).normalize();
                return new CaptureNoteResponse(id.toString(), existingFileName, existingPath.toString(), false);
            }

            String fileName = createFileName(id, createdAt);
            Path filePath = inboxPath.resolve(fileName).normalize();
            Files.writeString(filePath, toMarkdown(content, List.of(), List.of(), List.of(), createdAt), StandardCharsets.UTF_8);

            index.setProperty(id.toString(), fileName);
            storeIndex(index);

            return new CaptureNoteResponse(id.toString(), fileName, filePath.toString(), true);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write note capture", exception);
        }
    }

    public synchronized CaptureNoteResponse captureRich(RichCaptureRequest request, List<RichCaptureAttachment> attachments) {
        UUID id = parseId(request.id());
        String content = normalizeContent(request.content());
        List<String> links = normalizeList(request.links());
        List<String> categories = normalizeCategories(request.categories());
        List<RichCaptureAttachment> safeAttachments = attachments == null ? List.of() : attachments;
        Instant createdAt = parseCreatedAt(request.createdAt());

        if (content.isBlank() && links.isEmpty() && safeAttachments.isEmpty()) {
            throw new InvalidCaptureException("Capture content, link, or attachment is required");
        }

        try {
            prepareStorage();

            Properties index = loadIndex();
            String existingFileName = index.getProperty(id.toString());
            if (existingFileName != null) {
                Path existingPath = inboxPath.resolve(existingFileName).normalize();
                return new CaptureNoteResponse(id.toString(), existingFileName, existingPath.toString(), false);
            }

            List<String> imageLinks = writeAttachments(id, safeAttachments);
            String fileName = createFileName(id, createdAt);
            Path filePath = inboxPath.resolve(fileName).normalize();
            Files.writeString(
                    filePath,
                    toMarkdown(content, links, categories, imageLinks, createdAt),
                    StandardCharsets.UTF_8
            );

            index.setProperty(id.toString(), fileName);
            storeIndex(index);

            return new CaptureNoteResponse(id.toString(), fileName, filePath.toString(), true);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write rich capture", exception);
        }
    }

    private void prepareStorage() throws IOException {
        Files.createDirectories(inboxPath);
        Path indexParent = indexPath.getParent();
        if (indexParent != null) {
            Files.createDirectories(indexParent);
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
        String normalized = normalizeContent(content);
        if (normalized.isBlank()) {
            throw new InvalidCaptureException("Capture content is required");
        }
        return normalized;
    }

    private String normalizeContent(String content) {
        if (content == null) {
            return "";
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

    private List<String> normalizeList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::strip)
                .toList();
    }

    private List<String> normalizeCategories(List<String> values) {
        return normalizeList(values).stream()
                .map(value -> value.replace(" ", ""))
                .filter(value -> !value.isBlank())
                .toList();
    }

    private String normalizeAttachmentDirectory(String value) {
        if (value == null || value.isBlank()) {
            return "_attachments";
        }
        return SAFE_FILE_NAME.matcher(value.strip()).replaceAll("_");
    }

    private String createFileName(UUID id, Instant createdAt) {
        String suffix = id.toString().substring(0, 8);
        return FILE_TIMESTAMP.format(createdAt) + "-" + suffix + ".md";
    }

    private List<String> writeAttachments(UUID id, List<RichCaptureAttachment> attachments) throws IOException {
        if (attachments.isEmpty()) {
            return List.of();
        }

        Path attachmentRoot = inboxPath.resolve(attachmentDirectory).resolve(id.toString()).normalize();
        if (!attachmentRoot.startsWith(inboxPath.normalize())) {
            throw new InvalidCaptureException("Attachment path is unsafe");
        }
        Files.createDirectories(attachmentRoot);

        List<String> markdownLinks = new ArrayList<>();
        for (int index = 0; index < attachments.size(); index++) {
            RichCaptureAttachment attachment = attachments.get(index);
            validateAttachment(attachment);
            String fileName = safeAttachmentFileName(attachment.fileName(), index + 1);
            Path target = attachmentRoot.resolve(fileName).normalize();
            if (!target.startsWith(attachmentRoot)) {
                throw new InvalidCaptureException("Attachment file name is unsafe");
            }
            Files.write(target, attachment.content());
            markdownLinks.add("%s/%s/%s".formatted(attachmentDirectory, id, fileName));
        }
        return markdownLinks;
    }

    private void validateAttachment(RichCaptureAttachment attachment) {
        if (attachment == null || attachment.content() == null || attachment.content().length == 0) {
            throw new InvalidCaptureException("Attachment content is required");
        }
        String contentType = attachment.contentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new InvalidCaptureException("Only image attachments are supported");
        }
    }

    private String safeAttachmentFileName(String originalName, int index) {
        String fallback = "image-%d.jpg".formatted(index);
        String candidate = originalName == null || originalName.isBlank() ? fallback : Path.of(originalName).getFileName().toString();
        candidate = SAFE_FILE_NAME.matcher(candidate).replaceAll("_");
        if (candidate.isBlank() || candidate.equals(".") || candidate.equals("..")) {
            return fallback;
        }
        return candidate;
    }

    private String toMarkdown(
            String content,
            List<String> links,
            List<String> categories,
            List<String> imageLinks,
            Instant createdAt
    ) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("""
                ---
                created_at: "%s"
                synced_at: "%s"
                ---
                """.formatted(createdAt, Instant.now()));

        if (!content.isBlank()) {
            markdown.append("\n").append(content).append("\n");
        }
        if (!categories.isEmpty()) {
            markdown.append("\nTags: ");
            categories.forEach(category -> markdown.append("#").append(category).append(" "));
            markdown.append("\n");
        }
        if (!links.isEmpty()) {
            markdown.append("\nLinks:\n");
            links.forEach(link -> markdown.append("- ").append(link).append("\n"));
        }
        if (!imageLinks.isEmpty()) {
            markdown.append("\nImages:\n");
            imageLinks.forEach(link -> markdown.append("![](").append(link).append(")\n"));
        }
        return markdown.toString();
    }

    private String escapeYaml(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
