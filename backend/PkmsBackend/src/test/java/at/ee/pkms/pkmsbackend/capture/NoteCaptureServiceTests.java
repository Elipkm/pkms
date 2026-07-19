package at.ee.pkms.pkmsbackend.capture;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NoteCaptureServiceTests {

    @TempDir
    Path tempDir;

    @Test
    void writesMarkdownFileForNewCapture() throws Exception {
        NoteCaptureService service = new NoteCaptureService(properties());
        CaptureNoteRequest request = new CaptureNoteRequest(
                "55b90557-bd22-42af-a8ad-e666a3cb6c64",
                "Remember the PKMS MVP sync path.",
                "2026-07-19T10:15:30Z",
                "android"
        );

        CaptureNoteResponse response = service.capture(request);

        assertThat(response.created()).isTrue();
        assertThat(response.fileName()).isEqualTo("20260719-101530-55b90557.md");

        Path markdownFile = tempDir.resolve("Inbox").resolve(response.fileName());
        assertThat(Files.exists(markdownFile)).isTrue();
        assertThat(Files.readString(markdownFile))
                .contains("created_at: \"2026-07-19T10:15:30Z\"")
                .contains("synced_at:")
                .doesNotContain("capture_id:")
                .doesNotContain("sync_status:")
                .doesNotContain("source:")
                .contains("Remember the PKMS MVP sync path.");
    }

    @Test
    void writesMarkdownAndAttachmentForRichCapture() throws Exception {
        NoteCaptureService service = new NoteCaptureService(properties());
        RichCaptureRequest request = new RichCaptureRequest(
                "55b90557-bd22-42af-a8ad-e666a3cb6c64",
                "Photo from the workshop.",
                List.of(),
                List.of(),
                "2026-07-19T10:15:30Z"
        );

        CaptureNoteResponse response = service.captureRich(
                request,
                List.of(new RichCaptureAttachment("whiteboard.jpg", "image/jpeg", "image-bytes".getBytes(StandardCharsets.UTF_8)))
        );

        Path markdownFile = tempDir.resolve("Inbox").resolve(response.fileName());
        Path attachmentFile = tempDir.resolve("Inbox")
                .resolve("_attachments")
                .resolve(request.id())
                .resolve("whiteboard.jpg");

        assertThat(response.created()).isTrue();
        assertThat(Files.readString(markdownFile))
                .contains("Photo from the workshop.")
                .contains("![](_attachments/55b90557-bd22-42af-a8ad-e666a3cb6c64/whiteboard.jpg)")
                .doesNotContain("capture_id:");
        assertThat(Files.readString(attachmentFile)).isEqualTo("image-bytes");
    }

    @Test
    void writesLinksAndCategoriesOutsideFrontmatterForRichCapture() throws Exception {
        NoteCaptureService service = new NoteCaptureService(properties());
        RichCaptureRequest request = new RichCaptureRequest(
                "55b90557-bd22-42af-a8ad-e666a3cb6c64",
                "Read this later.",
                List.of("https://example.com/article"),
                List.of("Work", "TODO"),
                "2026-07-19T10:15:30Z"
        );

        CaptureNoteResponse response = service.captureRich(request, List.of());

        String markdown = Files.readString(tempDir.resolve("Inbox").resolve(response.fileName()));
        assertThat(markdown)
                .contains("created_at: \"2026-07-19T10:15:30Z\"")
                .contains("Tags: #Work #TODO")
                .contains("- https://example.com/article")
                .doesNotContain("categories:")
                .doesNotContain("links:");
    }

    @Test
    void returnsExistingFileForDuplicateCaptureId() {
        NoteCaptureService service = new NoteCaptureService(properties());
        CaptureNoteRequest request = new CaptureNoteRequest(
                "55b90557-bd22-42af-a8ad-e666a3cb6c64",
                "First body",
                "2026-07-19T10:15:30Z",
                "android"
        );

        CaptureNoteResponse firstResponse = service.capture(request);
        CaptureNoteResponse duplicateResponse = service.capture(
                new CaptureNoteRequest(
                        request.id(),
                        "Changed body",
                        request.createdAt(),
                        request.source()
                )
        );

        assertThat(firstResponse.created()).isTrue();
        assertThat(duplicateResponse.created()).isFalse();
        assertThat(duplicateResponse.fileName()).isEqualTo(firstResponse.fileName());
    }

    @Test
    void returnsExistingFileForDuplicateRichCaptureId() {
        NoteCaptureService service = new NoteCaptureService(properties());
        RichCaptureRequest request = new RichCaptureRequest(
                "55b90557-bd22-42af-a8ad-e666a3cb6c64",
                "First body",
                List.of(),
                List.of(),
                "2026-07-19T10:15:30Z"
        );

        CaptureNoteResponse firstResponse = service.captureRich(request, List.of());
        CaptureNoteResponse duplicateResponse = service.captureRich(
                new RichCaptureRequest(
                        request.id(),
                        "Changed body",
                        List.of("https://example.com/changed"),
                        List.of("Business"),
                        request.createdAt()
                ),
                List.of()
        );

        assertThat(firstResponse.created()).isTrue();
        assertThat(duplicateResponse.created()).isFalse();
        assertThat(duplicateResponse.fileName()).isEqualTo(firstResponse.fileName());
    }

    private NoteCaptureProperties properties() {
        NoteCaptureProperties properties = new NoteCaptureProperties();
        properties.getVault().setInboxPath(tempDir.resolve("Inbox"));
        properties.getSync().setIndexPath(tempDir.resolve("data").resolve("synced-captures.properties"));
        return properties;
    }
}
