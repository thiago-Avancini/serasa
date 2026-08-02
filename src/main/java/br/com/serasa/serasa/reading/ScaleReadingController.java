package br.com.serasa.serasa.reading;

import br.com.serasa.serasa.reading.dto.ScaleReadingRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fire-and-forget target for the ESP32 scales: they never read the response,
 * so this only needs to accept fast and validate enough to keep garbage out of the pipeline.
 */
@RestController
@RequestMapping("/api/scale-readings")
public class ScaleReadingController {

    private final ReadingIngestionService ingestionService;

    public ScaleReadingController(ReadingIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    public ResponseEntity<Void> receive(@Valid @RequestBody ScaleReadingRequest request) {
        ingestionService.ingest(request.id(), request.plate(), request.weight());
        return ResponseEntity.accepted().build();
    }
}