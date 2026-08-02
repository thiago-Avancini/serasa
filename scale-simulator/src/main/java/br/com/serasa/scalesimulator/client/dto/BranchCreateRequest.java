package br.com.serasa.scalesimulator.client.dto;

public record BranchCreateRequest(String code, String name, String city, String state) {
}