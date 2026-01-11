package store._0982.batch.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseChangedEvent;
import store._0982.common.kafka.dto.GroupPurchaseEvent;


@Configuration
public class GroupPurchaseKafkaProducerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, GroupPurchaseEvent> groupPurchaseProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootstrapServers);
    }

    @Bean
    public KafkaTemplate<String, GroupPurchaseEvent> groupPurchaseKafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(groupPurchaseProducerFactory());
    }

    @Bean
    public ProducerFactory<String, GroupPurchaseChangedEvent> groupPurchaseChangedProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootstrapServers);
    }

    @Bean
    public KafkaTemplate<String, GroupPurchaseChangedEvent> groupPurchaseChangedKafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(groupPurchaseChangedProducerFactory());
    }

    @Bean
    public NewTopic groupPurchaseTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.GROUP_PURCHASE_CREATED);
    }

    @Bean
    public NewTopic groupPurchaseChangedTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.GROUP_PURCHASE_CHANGED);
    }
}
