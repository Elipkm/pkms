package at.ee.pkms.pkmsbackend.capture;

public record CaptureNoteRequest(
        String id,
        String content,
        String createdAt,
        String source
) {
}
