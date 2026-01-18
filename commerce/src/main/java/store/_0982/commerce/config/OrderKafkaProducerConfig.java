package store._0982.commerce.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderCanceledEvent;

@Configuration
public class OrderKafkaProducerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, OrderCanceledEvent> orderCanceledEventProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootstrapServers);
    }

    @Bean
    public KafkaTemplate<String, OrderCanceledEvent> orderCanceledEventKafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(orderCanceledEventProducerFactory());
    }

    @Bean
    public NewTopic orderCancelTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.ORDER_CANCELED);
    }
}
