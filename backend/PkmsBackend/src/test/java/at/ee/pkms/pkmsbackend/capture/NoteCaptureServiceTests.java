package at.ee.pkms.pkmsbackend.capture;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

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
                .contains("capture_id: \"55b90557-bd22-42af-a8ad-e666a3cb6c64\"")
                .contains("sync_status: \"synced\"")
                .contains("Remember the PKMS MVP sync path.");
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

    private NoteCaptureProperties properties() {
        NoteCaptureProperties properties = new NoteCaptureProperties();
        properties.getVault().setInboxPath(tempDir.resolve("Inbox"));
        properties.getSync().setIndexPath(tempDir.resolve("data").resolve("synced-captures.properties"));
        return properties;
    }
}
