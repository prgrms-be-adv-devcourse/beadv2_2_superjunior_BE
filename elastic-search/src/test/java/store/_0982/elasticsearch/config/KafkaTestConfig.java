package store._0982.elasticsearch.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import store._0982.elasticsearch.application.support.KafkaTestProbe;

@TestConfiguration
public class KafkaTestConfig {

    @Bean
    public KafkaTestProbe kafkaTestProbe() {
        return new KafkaTestProbe();
    }
}
