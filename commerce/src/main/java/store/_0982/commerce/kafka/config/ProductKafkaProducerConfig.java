package store._0982.commerce.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductEvent;

@Configuration
public class ProductKafkaProducerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, ProductEvent> productProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootstrapServers);
    }

    @Bean
    public KafkaTemplate<String, ProductEvent> productKafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(productProducerFactory());
    }

    @Bean
    public NewTopic upsertProductTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.PRODUCT_UPSERTED);
    }

    @Bean
    public NewTopic deleteProductTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.PRODUCT_DELETED);
    }
}
