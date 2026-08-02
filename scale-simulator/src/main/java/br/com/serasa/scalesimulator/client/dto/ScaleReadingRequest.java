package br.com.serasa.scalesimulator.client.dto;

import java.math.BigDecimal;

/**
 * Mirrors the exact JSON the real ESP32 sends: {"id", "plate", "weight"}.
 */
public record ScaleReadingRequest(String id, String plate, BigDecimal weight) {
}