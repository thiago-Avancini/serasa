package br.com.serasa.scalesimulator.client.dto;

import java.math.BigDecimal;

public record TruckCreateRequest(String plate, BigDecimal tareWeightKg, BigDecimal cargoCapacityTons) {
}