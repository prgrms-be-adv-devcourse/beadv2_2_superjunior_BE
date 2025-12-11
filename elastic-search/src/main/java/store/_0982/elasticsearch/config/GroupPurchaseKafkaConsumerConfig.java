package store._0982.elasticsearch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.dto.GroupPurchaseEvent;


@Configuration
public class GroupPurchaseKafkaConsumerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, GroupPurchaseEvent> createGroupPurchaseConsumerFactory(){
        return KafkaCommonConfigs.defaultConsumerFactory(bootstrapServers, "search-service-group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, GroupPurchaseEvent> createGroupPurchaseKafkaListenerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(createGroupPurchaseConsumerFactory());
    }

//    @Bean
//    public ConsumerFactory<String, UUID> deleteProductConsumerFactory(){
//        return KafkaCommonConfigs.defaultConsumerFactory(bootstrapServers, "search-service-group");
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, UUID> deleteProductKafkaListenerFactory() {
//        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(deleteProductConsumerFactory());
//    }
}
