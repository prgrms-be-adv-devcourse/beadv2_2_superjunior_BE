package store._0982.point.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderCanceledEvent;
import store._0982.common.kafka.dto.OrderChangedEvent;
import store._0982.common.kafka.dto.PointChangedEvent;
import store._0982.point.common.KafkaGroupIds;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootStrapServer;

    @Bean
    public ProducerFactory<String, PointChangedEvent> producerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootStrapServer);
    }

    @Bean
    public KafkaTemplate<String, PointChangedEvent> kafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, OrderCanceledEvent> orderCanceledEventConsumerFactory() {
        return KafkaCommonConfigs.defaultConsumerFactory(bootStrapServer, KafkaGroupIds.PAYMENT_SERVICE);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCanceledEvent> orderCanceledEventListenerContainerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(orderCanceledEventConsumerFactory());
    }

    @Bean
    public ConsumerFactory<String, OrderChangedEvent> orderChangedEventConsumerFactory() {
        return KafkaCommonConfigs.defaultConsumerFactory(bootStrapServer, KafkaGroupIds.PAYMENT_SERVICE);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderChangedEvent> orderChangedEventListenerContainerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(orderChangedEventConsumerFactory());
    }

    @Bean
    public NewTopic pointRechargedTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.PAYMENT_CHANGED);
    }

    @Bean
    public NewTopic pointChangedTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.POINT_CHANGED);
    }
}
