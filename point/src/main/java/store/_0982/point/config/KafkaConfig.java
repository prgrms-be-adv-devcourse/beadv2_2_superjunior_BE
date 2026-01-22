package store._0982.point.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.BaseEvent;
import store._0982.common.kafka.dto.PaymentChangedEvent;
import store._0982.common.kafka.dto.PointChangedEvent;
import store._0982.point.common.KafkaGroupIds;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootStrapServer;

    @Bean
    @Primary
    public ProducerFactory<String, BaseEvent> baseEventProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootStrapServer);
    }

    @Bean
    @Primary
    public KafkaTemplate<String, BaseEvent> kafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(baseEventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, PointChangedEvent> pointChangedEventProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootStrapServer);
    }

    @Bean
    public KafkaTemplate<String, PointChangedEvent> pointChangedEventKafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(pointChangedEventProducerFactory());
    }

    @Bean
    ProducerFactory<String, PaymentChangedEvent> paymentChangedEventProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootStrapServer);
    }

    @Bean
    public KafkaTemplate<String, PaymentChangedEvent> paymentChangedEventKafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(paymentChangedEventProducerFactory());
    }


    @Bean
    public ConsumerFactory<String, BaseEvent> baseEventConsumerFactory() {
        return KafkaCommonConfigs.defaultConsumerFactory(bootStrapServer, KafkaGroupIds.PAYMENT_SERVICE);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BaseEvent> baseEventListenerContainerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(baseEventConsumerFactory());
    }

    @Bean
    public NewTopic paymentChangedTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.PAYMENT_CHANGED);
    }

    @Bean
    public NewTopic pointChangedTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.POINT_CHANGED);
    }
}
