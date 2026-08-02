package br.com.serasa.scalesimulator.client.dto;

import java.math.BigDecimal;

public record GrainTypeCreateRequest(String name, BigDecimal purchasePricePerTon, BigDecimal availableQuantityTons) {
}