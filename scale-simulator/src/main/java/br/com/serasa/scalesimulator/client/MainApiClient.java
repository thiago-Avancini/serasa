package br.com.serasa.scalesimulator.client;

import br.com.serasa.scalesimulator.client.dto.BranchCreateRequest;
import br.com.serasa.scalesimulator.client.dto.BranchDto;
import br.com.serasa.scalesimulator.client.dto.GrainTypeCreateRequest;
import br.com.serasa.scalesimulator.client.dto.GrainTypeDto;
import br.com.serasa.scalesimulator.client.dto.ScaleCreateRequest;
import br.com.serasa.scalesimulator.client.dto.ScaleDto;
import br.com.serasa.scalesimulator.client.dto.ScaleReadingRequest;
import br.com.serasa.scalesimulator.client.dto.StartTransactionRequest;
import br.com.serasa.scalesimulator.client.dto.TruckCreateRequest;
import br.com.serasa.scalesimulator.client.dto.TruckDto;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Thin client for the main Serasa API. Deliberately independent from the main app's own
 * DTOs: this module ships and deploys separately, so it talks to the API the same way any
 * external client (or the real ESP32, for the reading endpoint) would.
 */
public class MainApiClient {

    private static final Logger log = LoggerFactory.getLogger(MainApiClient.class);

    private final String baseUrl;
    private final RestClient restClient;

    public MainApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void waitUntilReady(Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            try {
                restClient.get().uri("/api/branches").retrieve().toBodilessEntity();
                return;
            } catch (RestClientException e) {
                log.info("Main API at {} not ready yet ({}), retrying...", baseUrl, e.getMessage());
                sleep(Duration.ofSeconds(2));
            }
        }
        throw new IllegalStateException("Main API at " + baseUrl + " was not reachable within " + timeout);
    }

    public List<BranchDto> listBranches() {
        return restClient.get().uri("/api/branches").retrieve()
                .body(new ParameterizedTypeReference<List<BranchDto>>() {
                });
    }

    public List<GrainTypeDto> listGrainTypes() {
        return restClient.get().uri("/api/grain-types").retrieve()
                .body(new ParameterizedTypeReference<List<GrainTypeDto>>() {
                });
    }

    public List<TruckDto> listTrucks() {
        return restClient.get().uri("/api/trucks").retrieve()
                .body(new ParameterizedTypeReference<List<TruckDto>>() {
                });
    }

    public List<ScaleDto> listScales() {
        return restClient.get().uri("/api/scales").retrieve()
                .body(new ParameterizedTypeReference<List<ScaleDto>>() {
                });
    }

    public Long ensureBranch(String code, String name) {
        List<BranchDto> branches = listBranches();
        return branches.stream()
                .filter(b -> b.code().equals(code))
                .map(BranchDto::id)
                .findFirst()
                .orElseGet(() -> {
                    BranchDto created = restClient.post().uri("/api/branches")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(new BranchCreateRequest(code, name, null, null))
                            .retrieve().body(BranchDto.class);
                    log.info("Created branch '{}' (id={})", code, created.id());
                    return created.id();
                });
    }

    public GrainTypeDto ensureGrainType(String name, BigDecimal purchasePricePerTon, BigDecimal availableQuantityTons) {
        List<GrainTypeDto> grainTypes = listGrainTypes();
        return grainTypes.stream()
                .filter(g -> g.name().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    GrainTypeDto created = restClient.post().uri("/api/grain-types")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(new GrainTypeCreateRequest(name, purchasePricePerTon, availableQuantityTons))
                            .retrieve().body(GrainTypeDto.class);
                    log.info("Created grain type '{}' (id={})", name, created.id());
                    return created;
                });
    }

    public TruckDto ensureTruck(String plate, BigDecimal tareWeightKg) {
        List<TruckDto> trucks = listTrucks();
        return trucks.stream()
                .filter(t -> t.plate().equalsIgnoreCase(plate))
                .findFirst()
                .orElseGet(() -> {
                    TruckDto created = restClient.post().uri("/api/trucks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(new TruckCreateRequest(plate, tareWeightKg, null))
                            .retrieve().body(TruckDto.class);
                    log.info("Created truck '{}' (id={})", created.plate(), created.id());
                    return created;
                });
    }

    public void ensureScale(String code, Long branchId) {
        List<ScaleDto> scales = listScales();
        boolean exists = scales.stream().anyMatch(s -> s.code().equals(code));
        if (!exists) {
            restClient.post().uri("/api/scales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ScaleCreateRequest(code, branchId, "Simulated scale", true))
                    .retrieve().toBodilessEntity();
            log.info("Created scale '{}'", code);
        }
    }

    /**
     * Starts a new transport transaction for the truck, the way a dispatcher would before
     * a real weighing. A 409 means the truck already has one open (e.g. the simulator was
     * restarted mid-cycle) — that's fine to proceed on, since readings are correlated by
     * plate on the server, not by transaction id.
     */
    public boolean startTransaction(Long truckId, Long grainTypeId, Long branchId) {
        try {
            restClient.post().uri("/api/transport-transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new StartTransactionRequest(truckId, grainTypeId, branchId))
                    .retrieve().toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.Conflict e) {
            return true;
        } catch (RestClientException e) {
            log.warn("Failed to start transport transaction for truck {}: {}", truckId, e.getMessage());
            return false;
        }
    }

    public void sendReading(String scaleCode, String plate, BigDecimal weightKg) {
        try {
            restClient.post().uri("/api/scale-readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ScaleReadingRequest(scaleCode, plate, weightKg))
                    .retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("[{}] failed to send reading for plate {}: {}", scaleCode, plate, e.getMessage());
        }
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}