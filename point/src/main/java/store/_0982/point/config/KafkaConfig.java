package store._0982.point.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PointEvent;

@Configuration
public class KafkaConfig {
    @Value("${kafka.bootstrap-servers}")
    private String bootStrapServer;

    @Bean
    public ProducerFactory<String, PointEvent> producerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootStrapServer);
    }

    @Bean
    public KafkaTemplate<String, PointEvent> kafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(producerFactory());
    }

    @Bean
    public NewTopic pointRechargedTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.POINT_RECHARGED);
    }

    @Bean
    public NewTopic pointChangedTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.POINT_CHANGED);
    }
}
