package br.com.serasa.serasa.reading;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Per-scale mutable state machine: IDLE -> ACCUMULATING -> STABILIZED -> (weight drops, or
 * readings go quiet for a while) -> IDLE. Only one truck can physically be on a given scale
 * at a time, so a single session per scale code is enough; methods are synchronized to
 * serialize readings for that scale.
 */
class ScaleSession {

    private enum Status { IDLE, ACCUMULATING, STABILIZED }

    private record WeightSample(BigDecimal weightKg, Instant capturedAt) {
    }

    private Status status = Status.IDLE;
    private final Deque<WeightSample> window = new ArrayDeque<>();
    private Instant lastReadingAt;

    synchronized SessionEvent addReading(BigDecimal weightKg, Instant now, StabilizationProperties properties) {
        if (status != Status.IDLE && lastReadingAt != null
                && Duration.between(lastReadingAt, now).compareTo(properties.staleAfter()) > 0) {
            // The ESP32 only stops sending once the truck actually leaves the scale (it keeps
            // sending every 100ms even after we've stabilized, while the truck just sits there).
            // A gap this large means the previous session is stale, regardless of how long it
            // took the truck to stabilize or physically drive off.
            reset();
        }
        lastReadingAt = now;

        if (weightKg.compareTo(properties.emptyThresholdKg()) < 0) {
            boolean wasActive = status != Status.IDLE;
            reset();
            return wasActive ? SessionEvent.RESET : SessionEvent.NONE;
        }

        if (status == Status.STABILIZED) {
            return SessionEvent.NONE;
        }

        boolean wasIdle = status == Status.IDLE;
        window.addLast(new WeightSample(weightKg, now));
        evictOlderThan(now, properties.windowDuration());

        if (wasIdle) {
            status = Status.ACCUMULATING;
            return SessionEvent.STARTED;
        }

        if (isStable(properties)) {
            status = Status.STABILIZED;
            return SessionEvent.STABILIZED;
        }

        return SessionEvent.NONE;
    }

    synchronized BigDecimal averageWeight() {
        BigDecimal sum = window.stream().map(WeightSample::weightKg).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(window.size()), 3, java.math.RoundingMode.HALF_UP);
    }

    private void evictOlderThan(Instant now, Duration windowDuration) {
        while (!window.isEmpty() && Duration.between(window.peekFirst().capturedAt(), now).compareTo(windowDuration) > 0) {
            window.pollFirst();
        }
    }

    private boolean isStable(StabilizationProperties properties) {
        if (window.size() < properties.minSamples()) {
            return false;
        }
        BigDecimal min = window.stream().map(WeightSample::weightKg).min(BigDecimal::compareTo).orElseThrow();
        BigDecimal max = window.stream().map(WeightSample::weightKg).max(BigDecimal::compareTo).orElseThrow();
        return max.subtract(min).compareTo(properties.toleranceKg()) <= 0;
    }

    private void reset() {
        status = Status.IDLE;
        window.clear();
    }
}