package at.fhtw.energy_usage_service;

public record EnergyMessage(String type, String association, double kwh, String datetime) {}
