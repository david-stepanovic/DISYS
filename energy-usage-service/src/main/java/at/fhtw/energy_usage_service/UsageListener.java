package at.fhtw.energy_usage_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Component
public class UsageListener {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final JdbcTemplate jdbc;
    private final RabbitTemplate rabbit;
    private final ObjectMapper mapper = new ObjectMapper();

    public UsageListener(JdbcTemplate jdbc, RabbitTemplate rabbit) {
        this.jdbc = jdbc;
        this.rabbit = rabbit;
    }

    @RabbitListener(queues = "energy.queue")
    public void handle(String json) throws Exception {
        EnergyMessage msg = mapper.readValue(json, EnergyMessage.class);
        LocalDateTime hour = LocalDateTime.parse(msg.datetime(), FORMATTER).truncatedTo(ChronoUnit.HOURS);
        Timestamp hourTs = Timestamp.valueOf(hour);

        if ("PRODUCER".equals(msg.type())) {
            jdbc.update("""
                INSERT INTO energy_usage(hour, community_produced, community_used, grid_used)
                VALUES (?, ?, 0, 0)
                ON CONFLICT (hour) DO UPDATE
                SET community_produced = energy_usage.community_produced + EXCLUDED.community_produced
                """, hourTs, msg.kwh());

        } else if ("USER".equals(msg.type())) {
            // Ensure the row exists before updating
            jdbc.update("""
                INSERT INTO energy_usage(hour, community_produced, community_used, grid_used)
                VALUES (?, 0, 0, 0)
                ON CONFLICT DO NOTHING
                """, hourTs);

            // community_used can never exceed community_produced; overflow goes to grid
            jdbc.update("""
                UPDATE energy_usage
                SET community_used = community_used + LEAST(?, community_produced - community_used),
                    grid_used      = grid_used      + GREATEST(0.0, ? - (community_produced - community_used))
                WHERE hour = ?
                """, msg.kwh(), msg.kwh(), hourTs);
        }

        rabbit.convertAndSend("energy.notification.queue", hour.format(FORMATTER));
    }
}
