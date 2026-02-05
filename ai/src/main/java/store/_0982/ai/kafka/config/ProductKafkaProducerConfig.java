package store._0982.ai.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductEmbeddingCompletedEvent;


@Configuration
public class ProductKafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, ProductEmbeddingCompletedEvent> productEmbeddingCompleteEventProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootstrapServers);
    }

    @Bean
    public KafkaTemplate<String, ProductEmbeddingCompletedEvent> productEmbeddingCompleteEventKafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(productEmbeddingCompleteEventProducerFactory());
    }

    @Bean
    public NewTopic productEmbeddingCompleteEventTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.PRODUCT_EMBEDDING_COMPLETED);
    }
}
