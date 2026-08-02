package br.com.serasa.serasa.reading;

import br.com.serasa.serasa.transport.TransportTransactionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ScaleStabilizationService {

    private final Map<String, ScaleSession> sessions = new ConcurrentHashMap<>();
    private final StabilizationProperties properties;
    private final TransportTransactionService transactionService;

    public ScaleStabilizationService(StabilizationProperties properties, TransportTransactionService transactionService) {
        this.properties = properties;
        this.transactionService = transactionService;
    }

    public void process(String scaleCode, String plate, BigDecimal weightKg) {
        ScaleSession session = sessions.computeIfAbsent(scaleCode, code -> new ScaleSession());
        SessionEvent event = session.addReading(weightKg, Instant.now(), properties);

        switch (event) {
            case STARTED -> transactionService.markWeighingStarted(plate, scaleCode);
            case STABILIZED -> transactionService.completeWeighing(plate, session.averageWeight());
            case NONE, RESET -> {
            }
        }
    }
}