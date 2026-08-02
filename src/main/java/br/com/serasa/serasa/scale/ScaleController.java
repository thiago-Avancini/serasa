package br.com.serasa.serasa.scale;

import br.com.serasa.serasa.scale.dto.ScaleRequest;
import br.com.serasa.serasa.scale.dto.ScaleResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scales")
public class ScaleController {

    private final ScaleService service;

    public ScaleController(ScaleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ScaleResponse> create(@Valid @RequestBody ScaleRequest request) {
        ScaleResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/scales/" + response.code())).body(response);
    }

    @GetMapping
    public List<ScaleResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{code}")
    public ScaleResponse findByCode(@PathVariable String code) {
        return service.findByCode(code);
    }

    @PutMapping("/{code}")
    public ScaleResponse update(@PathVariable String code, @Valid @RequestBody ScaleRequest request) {
        return service.update(code, request);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        service.delete(code);
        return ResponseEntity.noContent().build();
    }
}
