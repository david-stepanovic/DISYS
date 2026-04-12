package at.fhtw.energy_api;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    @GetMapping("/current")
    public CurrentEnergy getCurrent() {
        return new CurrentEnergy(100.00, 5.63);
    }

    @GetMapping("/historical")
    public List<HistoricalEnergy> getHistorical(
            @RequestParam String start,
            @RequestParam String end) {
        // Testdaten
        return List.of(
                new HistoricalEnergy("2025-01-10T14:00:00", 143.024, 130.101, 14.75),
                new HistoricalEnergy("2025-01-10T13:00:00", 120.500, 110.300, 12.50),
                new HistoricalEnergy("2025-01-10T12:00:00", 98.750, 95.200, 8.30)
        );
   }
    // Record anstatt standard klassen
    record CurrentEnergy(double community_depleted, double grid_portion) {}
    record HistoricalEnergy(String hour, double community_produced, double community_used, double grid_used) {}
}