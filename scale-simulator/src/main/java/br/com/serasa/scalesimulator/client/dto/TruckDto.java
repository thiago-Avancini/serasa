package br.com.serasa.scalesimulator.client.dto;

import java.math.BigDecimal;

public record TruckDto(Long id, String plate, BigDecimal tareWeightKg, BigDecimal cargoCapacityTons) {
}