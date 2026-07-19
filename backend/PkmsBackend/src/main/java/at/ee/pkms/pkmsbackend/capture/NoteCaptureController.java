package at.ee.pkms.pkmsbackend.capture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @ExceptionHandler(InvalidCaptureException.class)
    public ResponseEntity<String> handleInvalidCapture(InvalidCaptureException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}
