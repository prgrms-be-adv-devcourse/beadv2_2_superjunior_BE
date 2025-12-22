package store._0982.commerce.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.SettlementEvent;

@Configuration
public class SettlementKafkaProducerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, SettlementEvent> settlementProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootstrapServers);
    }

    @Bean
    public KafkaTemplate<String, SettlementEvent> settlementKafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(settlementProducerFactory());
    }

    @Bean
    public NewTopic monthlySettlementCompletedTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.MONTHLY_SETTLEMENT_COMPLETED);
    }

    @Bean
    public NewTopic monthlySettlementFailedTopic() {
        return KafkaCommonConfigs.createTopic(KafkaTopics.MONTHLY_SETTLEMENT_FAILED);
    }

}
