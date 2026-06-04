package at.fhtw.energy_user;

// Record für Message
public record EnergyMessage(String type, String association, double kwh, String datetime) {
}
