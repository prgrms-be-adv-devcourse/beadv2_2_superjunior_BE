package store._0982.commerce.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.KafkaTopics;


@Configuration
public class ProductKafkaProducerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, ProductEmbeddingEvent> productEmbeddingEventProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootstrapServers);
    }

    @Bean
    public KafkaTemplate<String, ProductEmbeddingEvent> productEmbeddingEventKafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(productEmbeddingEventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, ProductEmbeddingCompleteEvent> productEmbeddingCompleteEventProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootstrapServers);
    }

    @Bean(name = "defaultRetryTopicKafkaTemplate")
    public KafkaTemplate<String, ProductEmbeddingCompleteEvent> productEmbeddingCompleteEventKafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(productEmbeddingCompleteEventProducerFactory());
    }

    @Bean
    public NewTopic productEmbeddingEventTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.PRODUCT_EMBEDDING_REQUESTED);
    }
}
