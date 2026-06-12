package at.fhtw.energy_percentage_service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class PercentageListener {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final JdbcTemplate jdbc;

    public PercentageListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @RabbitListener(queues = "energy.notification.queue")
    public void handle(String hourStr) {
        LocalDateTime hour = LocalDateTime.parse(hourStr, FORMATTER);

        Map<String, Object> row = jdbc.queryForMap(
            "SELECT community_produced, community_used, grid_used FROM energy_usage WHERE hour = ?",
            Timestamp.valueOf(hour));

        double produced  = ((Number) row.get("community_produced")).doubleValue();
        double used      = ((Number) row.get("community_used")).doubleValue();
        double gridUsed  = ((Number) row.get("grid_used")).doubleValue();

        double communityDepleted = produced > 0 ? (used / produced) * 100.0 : 0.0;
        double gridPortion       = (used + gridUsed) > 0 ? (gridUsed / (used + gridUsed)) * 100.0 : 0.0;

        // Table only holds the current hour — replace on every update
        jdbc.update("DELETE FROM energy_percentage");
        jdbc.update(
            "INSERT INTO energy_percentage(hour, community_depleted, grid_portion) VALUES(?, ?, ?)",
            Timestamp.valueOf(hour), round(communityDepleted), round(gridPortion));
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
