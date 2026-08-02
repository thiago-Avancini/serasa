package br.com.serasa.serasa.graintype;

import br.com.serasa.serasa.graintype.dto.GrainTypeRequest;
import br.com.serasa.serasa.graintype.dto.GrainTypeResponse;
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
@RequestMapping("/api/grain-types")
public class GrainTypeController {

    private final GrainTypeService service;

    public GrainTypeController(GrainTypeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<GrainTypeResponse> create(@Valid @RequestBody GrainTypeRequest request) {
        GrainTypeResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/grain-types/" + response.id())).body(response);
    }

    @GetMapping
    public List<GrainTypeResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public GrainTypeResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public GrainTypeResponse update(@PathVariable Long id, @Valid @RequestBody GrainTypeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
