package at.ee.pkms.pkmsbackend.capture;

public record RichCaptureAttachment(
        String fileName,
        String contentType,
        byte[] content
) {
}
