package at.ee.pkms.pkmsbackend.capture;

import java.util.List;

public record RichCaptureRequest(
        String id,
        String content,
        List<String> links,
        List<String> categories,
        String createdAt
) {
}
