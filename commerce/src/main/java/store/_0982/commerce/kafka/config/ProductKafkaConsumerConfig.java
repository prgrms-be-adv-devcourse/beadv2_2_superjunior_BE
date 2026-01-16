package store._0982.commerce.kafka.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.dto.ProductEmbeddingCompleteEvent;


@Configuration
public class ProductKafkaConsumerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, ProductEmbeddingCompleteEvent> productEmbeddingCompleteEventConsumerFactory(){
        return KafkaCommonConfigs.defaultConsumerFactory(bootstrapServers, "ai-service-group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductEmbeddingCompleteEvent> productEmbeddingCompleteEventKafkaListenerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(productEmbeddingCompleteEventConsumerFactory());
    }
}
