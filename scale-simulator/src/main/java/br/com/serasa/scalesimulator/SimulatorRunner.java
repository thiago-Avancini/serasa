package br.com.serasa.scalesimulator;

import br.com.serasa.scalesimulator.client.MainApiClient;
import br.com.serasa.scalesimulator.client.dto.GrainTypeDto;
import br.com.serasa.scalesimulator.client.dto.ScaleDto;
import br.com.serasa.scalesimulator.client.dto.TruckDto;
import br.com.serasa.scalesimulator.config.SimulatorProperties;
import br.com.serasa.scalesimulator.simulation.DemoScale;
import br.com.serasa.scalesimulator.simulation.ScaleWorker;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Keeps the running simulated workers in sync with what's actually registered in the main
 * app. Every {@code simulator.discovery-interval}, it re-lists active scales: any scale not
 * yet simulated gets a new {@link ScaleWorker} (with its own pool of trucks to rotate
 * through) on its own virtual thread, and any scale that disappeared (deleted) or was
 * deactivated gets its worker interrupted and dropped. This means a scale registered while
 * the simulator is already running gets picked up automatically, and one that's deleted
 * stops being hammered with readings instead of looping on 404s forever.
 */
@Component
public class SimulatorRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SimulatorRunner.class);

    private final SimulatorProperties properties;
    private final ExecutorService workerExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final Map<String, RunningWorker> runningWorkers = new ConcurrentHashMap<>();

    public SimulatorRunner(SimulatorProperties properties) {
        this.properties = properties;
    }

    /**
     * A truck can only have one open transport transaction at a time, so a truck must never
     * appear in two scales' pools at once — tracking the {@link DemoScale} here (not just the
     * {@link Future}) is what lets {@link #reconcile} tell which trucks are already spoken for.
     */
    private record RunningWorker(Future<?> future, DemoScale demoScale) {
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        MainApiClient client = new MainApiClient(properties.targetBaseUrl());

        log.info("Waiting for main API at {}...", properties.targetBaseUrl());
        client.waitUntilReady(Duration.ofMinutes(2));

        bootstrapIfEmpty(client);

        while (!Thread.currentThread().isInterrupted()) {
            reconcile(client);
            Thread.sleep(properties.discoveryInterval());
        }
    }

    /**
     * Only creates demo cadastros (from this module's own {@code simulator.scales} config)
     * when there isn't a single scale registered yet — an empty database is the one case
     * where there's nothing real to discover. Does not start any workers itself; the
     * reconcile loop picks up whatever ends up registered, including what this creates.
     */
    private void bootstrapIfEmpty(MainApiClient client) {
        List<ScaleDto> scales = client.listScales().stream().filter(ScaleDto::active).toList();
        if (!scales.isEmpty()) {
            return;
        }
        if (properties.scales().isEmpty()) {
            log.warn("No scales are registered in the main app, and 'simulator.scales' is empty in this "
                    + "module's own config — nothing to bootstrap. Register a scale through the API and it "
                    + "will be picked up within {}.", properties.discoveryInterval());
            return;
        }
        log.info("No scales registered yet in the main app — bootstrapping demo cadastros "
                + "from this module's own configuration ({} entries).", properties.scales().size());
        properties.scales().forEach(config -> bootstrapFromConfig(client, config));
    }

    private void reconcile(MainApiClient client) {
        List<ScaleDto> activeScales = client.listScales().stream().filter(ScaleDto::active).toList();
        Set<String> activeCodes = activeScales.stream().map(ScaleDto::code).collect(Collectors.toSet());

        runningWorkers.keySet().stream()
                .filter(code -> !activeCodes.contains(code))
                .toList()
                .forEach(code -> {
                    log.info("Scale '{}' is no longer registered/active — stopping its simulated worker.", code);
                    RunningWorker worker = runningWorkers.remove(code);
                    if (worker != null) {
                        worker.future().cancel(true);
                    }
                });

        List<ScaleDto> newScales = activeScales.stream()
                .filter(scale -> !runningWorkers.containsKey(scale.code()))
                .toList();
        if (newScales.isEmpty()) {
            return;
        }

        List<TruckDto> trucks = client.listTrucks();
        if (trucks.isEmpty()) {
            log.warn("Scales are registered but no trucks are — creating one demo truck so the simulation can run.");
            SimulatorProperties.ScaleConfig template = firstConfigOrFallback();
            trucks = List.of(client.ensureTruck(template.truckPlate(), template.truckTareKg()));
        }

        List<GrainTypeDto> grainTypes = client.listGrainTypes();
        if (grainTypes.isEmpty()) {
            log.warn("Scales are registered but no grain types are — creating one demo grain type so the simulation can run.");
            SimulatorProperties.ScaleConfig template = firstConfigOrFallback();
            grainTypes = List.of(client.ensureGrainType(
                    template.grainTypeName(), template.grainPurchasePricePerTon(), template.grainAvailableQuantityTons()));
        }

        // A truck can't be on two scales at once: only distribute trucks not already held by
        // a running worker's pool, split round-robin across the new scales so pools stay
        // disjoint. Any scale left without a truck is skipped and retried next pass.
        Set<String> assignedPlates = runningWorkers.values().stream()
                .flatMap(worker -> worker.demoScale().trucks().stream())
                .map(TruckDto::plate)
                .collect(Collectors.toCollection(HashSet::new));
        List<TruckDto> freeTrucks = trucks.stream()
                .filter(truck -> !assignedPlates.contains(truck.plate()))
                .toList();

        if (freeTrucks.isEmpty()) {
            log.warn("No free trucks left to assign to {} new scale(s) (all {} registered truck(s) are "
                    + "already in a running worker's pool) — will retry on the next discovery pass.",
                    newScales.size(), trucks.size());
            return;
        }

        Map<String, List<TruckDto>> poolsByScaleCode = new HashMap<>();
        for (int i = 0; i < freeTrucks.size(); i++) {
            String scaleCode = newScales.get(i % newScales.size()).code();
            poolsByScaleCode.computeIfAbsent(scaleCode, key -> new ArrayList<>()).add(freeTrucks.get(i));
        }

        for (ScaleDto scale : newScales) {
            List<TruckDto> pool = poolsByScaleCode.get(scale.code());
            if (pool == null || pool.isEmpty()) {
                log.warn("No truck available to assign to scale '{}' — will retry on the next discovery pass.", scale.code());
                continue;
            }
            DemoScale demoScale = new DemoScale(scale.code(), scale.branchId(), pool, grainTypes);
            log.info("New scale discovered: '{}' — starting simulated worker with a pool of {} truck(s): {}.",
                    scale.code(), pool.size(), pool.stream().map(TruckDto::plate).toList());
            Future<?> future = workerExecutor.submit(new ScaleWorker(demoScale, properties, client));
            runningWorkers.put(scale.code(), new RunningWorker(future, demoScale));
        }
    }

    private void bootstrapFromConfig(MainApiClient client, SimulatorProperties.ScaleConfig config) {
        Long branchId = client.ensureBranch(config.branchCode(), config.branchName());
        client.ensureGrainType(
                config.grainTypeName(), config.grainPurchasePricePerTon(), config.grainAvailableQuantityTons());
        client.ensureTruck(config.truckPlate(), config.truckTareKg());
        client.ensureScale(config.scaleCode(), branchId);
    }

    private SimulatorProperties.ScaleConfig firstConfigOrFallback() {
        if (!properties.scales().isEmpty()) {
            return properties.scales().getFirst();
        }
        return new SimulatorProperties.ScaleConfig(
                "SIMULATOR-FALLBACK", "SIM-FALLBACK", "Simulator Fallback Branch",
                "SIM1A23", BigDecimal.valueOf(9000),
                "Simulated Grain", BigDecimal.valueOf(100), BigDecimal.valueOf(100));
    }
}