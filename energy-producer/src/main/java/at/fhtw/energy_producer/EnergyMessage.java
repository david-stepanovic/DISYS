package at.fhtw.energy_producer;

// record für den Aufbau der Message
public record EnergyMessage(String type, String association, double kwh, String datetime) {
}
