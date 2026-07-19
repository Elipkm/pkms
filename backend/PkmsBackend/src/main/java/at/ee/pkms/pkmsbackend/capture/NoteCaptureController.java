package at.ee.pkms.pkmsbackend.capture;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/captures")
public class NoteCaptureController {

    private final NoteCaptureService service;

    public NoteCaptureController(NoteCaptureService service) {
        this.service = service;
    }

    @PostMapping("/notes")
    public CaptureNoteResponse createNote(@RequestBody CaptureNoteRequest request) {
        return service.capture(request);
    }

    @PostMapping(path = "/rich", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CaptureNoteResponse createRichCapture(
            @RequestPart("capture") RichCaptureRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws IOException {
        List<RichCaptureAttachment> attachments = files == null ? List.of() : files.stream()
                .map(file -> {
                    try {
                        return new RichCaptureAttachment(
                                file.getOriginalFilename(),
                                file.getContentType(),
                                file.getBytes()
                        );
                    } catch (IOException exception) {
                        throw new IllegalStateException("Could not read attachment", exception);
                    }
                })
                .toList();
        return service.captureRich(request, attachments);
    }

    @ExceptionHandler(InvalidCaptureException.class)
    public ResponseEntity<String> handleInvalidCapture(InvalidCaptureException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}
