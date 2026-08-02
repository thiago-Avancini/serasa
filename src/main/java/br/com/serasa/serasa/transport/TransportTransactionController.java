package br.com.serasa.serasa.transport;

import br.com.serasa.serasa.transport.dto.StartTransportTransactionRequest;
import br.com.serasa.serasa.transport.dto.TransportTransactionResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * No PUT/DELETE here on purpose: fields on a transport transaction (weight, cost, status)
 * are only ever mutated by the weighing/stabilization flow, not by direct client edits.
 */
@RestController
@RequestMapping("/api/transport-transactions")
public class TransportTransactionController {

    private final TransportTransactionService service;

    public TransportTransactionController(TransportTransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TransportTransactionResponse> start(@Valid @RequestBody StartTransportTransactionRequest request) {
        TransportTransactionResponse response = service.start(request);
        return ResponseEntity.created(URI.create("/api/transport-transactions/" + response.id())).body(response);
    }

    @GetMapping
    public List<TransportTransactionResponse> findAll(@RequestParam(required = false) TransactionStatus status) {
        return service.findAll(status);
    }

    @GetMapping("/{id}")
    public TransportTransactionResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }
}
