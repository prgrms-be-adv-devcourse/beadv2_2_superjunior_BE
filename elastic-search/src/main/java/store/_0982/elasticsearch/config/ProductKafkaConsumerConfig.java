package store._0982.elasticsearch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.dto.ProductEvent;

import java.util.UUID;

@Configuration
public class ProductKafkaConsumerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, ProductEvent> productUpsertConsumerFactory(){
        return KafkaCommonConfigs.defaultConsumerFactory(bootstrapServers, "search-service-group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductEvent> productUpsertKafkaListenerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(productUpsertConsumerFactory());
    }

    @Bean
    public ConsumerFactory<String, UUID> productDeleteConsumerFactory(){
        return KafkaCommonConfigs.defaultConsumerFactory(bootstrapServers, "search-service-group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UUID> productDeleteKafkaListenerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(productDeleteConsumerFactory());
    }
}
