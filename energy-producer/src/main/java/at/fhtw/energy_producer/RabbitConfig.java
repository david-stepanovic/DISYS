package at.fhtw.energy_producer;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Queue für RabbitMQ
@Configuration
public class RabbitConfig {

    @Bean
    public Queue energyQueue() {
        return new Queue("energy.queue", true); // durable = true
    }
}
