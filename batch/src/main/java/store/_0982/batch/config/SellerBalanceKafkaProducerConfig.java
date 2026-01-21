package store._0982.batch.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.SellerBalanceChangedEvent;

@Configuration
public class SellerBalanceKafkaProducerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, SellerBalanceChangedEvent> sellerBalanceProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootstrapServers);
    }

    @Bean
    public KafkaTemplate<String, SellerBalanceChangedEvent> sellerBalanceKafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(sellerBalanceProducerFactory());
    }

    @Bean
    public NewTopic sellerBalanceChangedTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.SELLER_BALANCE_CHANGED);
    }
}
