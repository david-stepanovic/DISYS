package at.fhtw.energy_percentage_service;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue notificationQueue() {
        return new Queue("energy.notification.queue", true);
    }
}
