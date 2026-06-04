package at.fhtw.energy_producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

//Energy producer
@Component
public class ProducerRunner implements CommandLineRunner {

    private static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final long WEATHER_REFRESH_MS = 5 * 60 * 1000; // alle 5 minuten

    private final RabbitTemplate rabbitTemplate;
    private final WeatherClient weatherClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Random random = new Random();

    public ProducerRunner(RabbitTemplate rabbitTemplate, WeatherClient weatherClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.weatherClient = weatherClient;
    }

    @Override
    public void run(String... args) throws Exception {
        double radiation = weatherClient.getSolarRadiation();
        long lastWeatherFetch = System.currentTimeMillis();

        while (true) {
            if (System.currentTimeMillis() - lastWeatherFetch > WEATHER_REFRESH_MS) {
                radiation = weatherClient.getSolarRadiation();
                lastWeatherFetch = System.currentTimeMillis();
            }

            double kwh = calculateKwh(radiation);
            EnergyMessage message = new EnergyMessage(
                    "PRODUCER",
                    "COMMUNITY",
                    kwh,
                    LocalDateTime.now().format(DATETIME_FORMAT));

            String json = mapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend("energy.queue", json);
            System.out.println("Sent: " + json);

            Thread.sleep(1000 + random.nextInt(4000)); // 1 bis 5 sec
        }
    }

    // Radiation von API zu kwh
    private double calculateKwh(double radiation) {
        double base = (radiation / 1000.0) * 0.05;       // scaling zu plausiblem Wert
        double jitter = 0.8 + random.nextDouble() * 0.4;  // 20% varianz
        double kwh = base * jitter;
        return Math.round(kwh * 1000.0) / 1000.0;         // 3 nachkommastellen
    }
}
