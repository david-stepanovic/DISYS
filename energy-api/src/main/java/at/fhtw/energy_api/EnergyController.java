package at.fhtw.energy_api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final JdbcTemplate jdbc;

    public EnergyController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/current")
    public Map<String, Object> getCurrent() {
        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT hour, community_depleted, grid_portion FROM energy_percentage LIMIT 1");
        if (rows.isEmpty()) {
            // Anstatt fehler 0 zurückgeben wenn keine Daten vorhanden sind
            return Map.of("community_depleted", 0.0, "grid_portion", 0.0);
        }
        return rows.get(0);
    }

    @GetMapping("/historical")
    public List<Map<String, Object>> getHistorical(
            @RequestParam String start,
            @RequestParam String end) {
        Timestamp from = Timestamp.valueOf(LocalDateTime.parse(start, FORMATTER));
        Timestamp to   = Timestamp.valueOf(LocalDateTime.parse(end,   FORMATTER));
        return jdbc.queryForList(
            "SELECT hour, community_produced, community_used, grid_used FROM energy_usage WHERE hour >= ? AND hour <= ? ORDER BY hour",
            from, to);
    }
}
