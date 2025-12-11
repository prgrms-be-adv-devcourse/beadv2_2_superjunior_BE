package store._0982.product.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseEvent;

@Configuration
public class GroupPurchaseKafkaProducerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, GroupPurchaseEvent> upsertGroupPurchaseProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootstrapServers);
    }

    @Bean
    public KafkaTemplate<String, GroupPurchaseEvent> upsertGroupPurchaseKafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(upsertGroupPurchaseProducerFactory());
    }

    @Bean
    public NewTopic upsertGroupPurchaseTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.GROUP_PURCHASE_ADDED);
    }

//    @Bean
//    public ProducerFactory<String, UUID> deleteGroupPurchaseProducerFactory() {
//        return KafkaCommonConfigs.defaultProducerFactory(bootstrapServers);
//    }
//
//    @Bean
//    public KafkaTemplate<String, UUID> deleteGroupPurchaseKafkaTemplate() {
//        return KafkaCommonConfigs.defaultKafkaTemplate(deleteGroupPurchaseProducerFactory());
//    }
//
//    @Bean
//    public NewTopic deleteGroupPurchaseTopic() {
//        return KafkaCommonConfigs.createTopic(KafkaTopics.GROUP_PURCHASE_STATUS_CHANGED);
//    }
}
