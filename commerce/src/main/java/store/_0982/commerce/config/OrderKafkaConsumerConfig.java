package store._0982.commerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.dto.PointChangedEvent;

@Configuration
public class OrderKafkaConsumerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, PointChangedEvent> orderConsumerFactory() {
        return KafkaCommonConfigs.defaultConsumerFactory(bootstrapServers, "order-service-group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PointChangedEvent> orderKafkaListenerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(orderConsumerFactory());
    }
}
