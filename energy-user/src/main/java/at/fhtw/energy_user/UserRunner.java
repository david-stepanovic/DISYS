package at.fhtw.energy_user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;


@Component
public class UserRunner implements CommandLineRunner {

    private static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Random random = new Random();

    public UserRunner(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        while (true) {
            double kwh = calculateKwh(LocalDateTime.now().getHour());
            EnergyMessage message = new EnergyMessage(
                    "USER",
                    "COMMUNITY",
                    kwh,
                    LocalDateTime.now().format(DATETIME_FORMAT));

            String json = mapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend("energy.queue", json);
            System.out.println("Sent: " + json);

            Thread.sleep(1000 + random.nextInt(4000)); // 1 bis 5 sec
        }
    }

    // Tageszeit
    private double calculateKwh(int hour) {
        double factor = demandFactor(hour);
        double base = factor * 0.05;                     // scaling zu plausiblem Wert
        double jitter = 0.8 + random.nextDouble() * 0.4;   // 20% varianz
        double kwh = base * jitter;
        return Math.round(kwh * 1000.0) / 1000.0;          // 3 nachkommastellen
    }

    // Factor je nach Tageszeit
    private double demandFactor(int hour) {
        if (hour >= 6 && hour <= 9) {
            return 1.0;
        } else if (hour >= 17 && hour <= 21) {
            return 1.0;
        } else if (hour >= 10 && hour <= 16) {
            return 0.5;
        } else {
            return 0.2;
        }
    }
}
