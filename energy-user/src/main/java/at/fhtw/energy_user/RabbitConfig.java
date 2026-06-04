package at.fhtw.energy_user;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Deklariert queue
@Configuration
public class RabbitConfig {

    @Bean
    public Queue energyQueue() {
        // durable = true so the queue survives a broker restart
        return new Queue("energy.queue", true);
    }
}
