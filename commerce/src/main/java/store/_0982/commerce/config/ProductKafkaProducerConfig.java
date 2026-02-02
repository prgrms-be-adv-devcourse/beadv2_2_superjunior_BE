package store._0982.commerce.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductEmbeddingCompletedEvent;
import store._0982.common.kafka.dto.ProductUpsertedEvent;


@Configuration
public class ProductKafkaProducerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, ProductUpsertedEvent> productEmbeddingEventProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootstrapServers);
    }

    @Bean
    public KafkaTemplate<String, ProductUpsertedEvent> productEmbeddingEventKafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(productEmbeddingEventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, ProductEmbeddingCompletedEvent> productEmbeddingCompleteEventProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootstrapServers);
    }

    @Bean(name = "defaultRetryTopicKafkaTemplate")
    public KafkaTemplate<String, ProductEmbeddingCompletedEvent> productEmbeddingCompleteEventKafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(productEmbeddingCompleteEventProducerFactory());
    }

    @Bean
    public NewTopic productEmbeddingEventTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.PRODUCT_UPSERTED);
    }
}
