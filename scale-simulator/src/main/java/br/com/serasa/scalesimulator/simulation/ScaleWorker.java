package br.com.serasa.scalesimulator.simulation;

import br.com.serasa.scalesimulator.client.MainApiClient;
import br.com.serasa.scalesimulator.client.dto.GrainTypeDto;
import br.com.serasa.scalesimulator.client.dto.TruckDto;
import br.com.serasa.scalesimulator.config.SimulatorProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulates one physical scale serving a queue of trucks: each cycle picks a random truck
 * and grain type from the scale's pool (like a different truck pulling up each time), opens
 * a transport transaction (like a dispatcher would), then drives a realistic weight curve —
 * noisy ramp-up as the truck drives onto the scale, narrowing until stabilized, a plateau
 * while it sits there (queue, paperwork), then a ramp-down as it drives off — sending a
 * reading every {@code readingInterval} the whole time, matching the ESP32's fire-and-forget
 * behavior. Runs forever, one cycle after another, until the executor is shut down.
 */
public class ScaleWorker implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ScaleWorker.class);

    private static final int RAMP_UP_TICKS = 15;
    private static final int SETTLE_TICKS = 15;
    private static final int RAMP_DOWN_TICKS = 10;
    private static final double STABILIZATION_TOLERANCE_KG = 20.0;

    private final DemoScale demoScale;
    private final SimulatorProperties properties;
    private final MainApiClient client;

    private String currentPlate;

    public ScaleWorker(DemoScale demoScale, SimulatorProperties properties, MainApiClient client) {
        this.demoScale = demoScale;
        this.properties = properties;
        this.client = client;
    }

    @Override
    public void run() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        while (!Thread.currentThread().isInterrupted()) {
            TruckDto truck = pickRandom(random, demoScale.trucks());
            GrainTypeDto grainType = pickRandom(random, demoScale.grainTypes());
            currentPlate = truck.plate();

            boolean started = client.startTransaction(truck.id(), grainType.id(), demoScale.branchId());
            if (!started) {
                sleepSeconds(random.nextInt(properties.idle().minSeconds(), properties.idle().maxSeconds() + 1));
                continue;
            }

            double targetKg = random.nextDouble(
                    properties.weight().targetMinKg().doubleValue(),
                    properties.weight().targetMaxKg().doubleValue());
            log.info("[{}] truck {} arriving, target gross weight ~{} kg",
                    demoScale.scaleCode(), currentPlate, Math.round(targetKg));

            rampUp(random, targetKg);
            settle(random, targetKg);
            int plateauTicks = random.nextInt(properties.plateau().minSeconds(), properties.plateau().maxSeconds() + 1)
                    * ticksPerSecond();
            plateau(random, targetKg, plateauTicks);
            rampDown(random, targetKg);

            int idleSeconds = random.nextInt(properties.idle().minSeconds(), properties.idle().maxSeconds() + 1);
            log.info("[{}] truck {} left the scale, idle for {}s", demoScale.scaleCode(), currentPlate, idleSeconds);
            sleepSeconds(idleSeconds);
        }
    }

    private static <T> T pickRandom(ThreadLocalRandom random, List<T> items) {
        return items.get(random.nextInt(items.size()));
    }

    private void rampUp(ThreadLocalRandom random, double targetKg) {
        for (int i = 0; i < RAMP_UP_TICKS; i++) {
            double progress = (i + 1) / (double) RAMP_UP_TICKS;
            double amplitude = lerp(targetKg * 0.05, targetKg * 0.005, progress);
            sendTick(Math.max(0, targetKg * progress + noise(random, amplitude)));
        }
    }

    private void settle(ThreadLocalRandom random, double targetKg) {
        for (int i = 0; i < SETTLE_TICKS; i++) {
            double progress = (i + 1) / (double) SETTLE_TICKS;
            double amplitude = lerp(targetKg * 0.005, STABILIZATION_TOLERANCE_KG * 0.4, progress);
            sendTick(Math.max(0, targetKg + noise(random, amplitude)));
        }
    }

    private void plateau(ThreadLocalRandom random, double targetKg, int ticks) {
        for (int i = 0; i < ticks; i++) {
            sendTick(targetKg + noise(random, STABILIZATION_TOLERANCE_KG * 0.3));
        }
    }

    private void rampDown(ThreadLocalRandom random, double targetKg) {
        for (int i = 0; i < RAMP_DOWN_TICKS; i++) {
            double progress = (i + 1) / (double) RAMP_DOWN_TICKS;
            sendTick(Math.max(0, targetKg * (1 - progress) + noise(random, targetKg * 0.01)));
        }
    }

    private void sendTick(double weightKg) {
        BigDecimal rounded = BigDecimal.valueOf(weightKg).setScale(2, RoundingMode.HALF_UP);
        client.sendReading(demoScale.scaleCode(), currentPlate, rounded);
        sleepMillis(properties.readingInterval().toMillis());
    }

    private int ticksPerSecond() {
        long intervalMs = properties.readingInterval().toMillis();
        return (int) Math.max(1, 1000 / intervalMs);
    }

    private static double noise(ThreadLocalRandom random, double amplitude) {
        return random.nextDouble(-amplitude, amplitude);
    }

    private static double lerp(double from, double to, double progress) {
        return from + (to - from) * progress;
    }

    private void sleepSeconds(int seconds) {
        sleepMillis(seconds * 1000L);
    }

    private void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
