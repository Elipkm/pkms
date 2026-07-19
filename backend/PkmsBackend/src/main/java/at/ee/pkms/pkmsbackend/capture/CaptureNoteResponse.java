package at.ee.pkms.pkmsbackend.capture;

public record CaptureNoteResponse(
        String id,
        String fileName,
        String filePath,
        boolean created
) {
}
