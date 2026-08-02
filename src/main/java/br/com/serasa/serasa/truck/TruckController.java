package br.com.serasa.serasa.truck;

import br.com.serasa.serasa.truck.dto.TruckRequest;
import br.com.serasa.serasa.truck.dto.TruckResponse;
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
@RequestMapping("/api/trucks")
public class TruckController {

    private final TruckService service;

    public TruckController(TruckService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TruckResponse> create(@Valid @RequestBody TruckRequest request) {
        TruckResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/trucks/" + response.id())).body(response);
    }

    @GetMapping
    public List<TruckResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TruckResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public TruckResponse update(@PathVariable Long id, @Valid @RequestBody TruckRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
