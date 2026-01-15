package store._0982.ai.kafka.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.dto.ProductEmbeddingEvent;


@Configuration
public class ProductKafkaConsumerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, ProductEmbeddingEvent> productEmbeddingEventConsumerFactory(){
        return KafkaCommonConfigs.defaultConsumerFactory(bootstrapServers, "ai-service-group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductEmbeddingEvent> productEmbeddingEventKafkaListenerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(productEmbeddingEventConsumerFactory());
    }
}
