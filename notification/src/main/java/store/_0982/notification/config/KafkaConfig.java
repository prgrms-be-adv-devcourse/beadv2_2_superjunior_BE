package store._0982.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.dto.BaseEvent;
import store._0982.notification.common.KafkaGroupIds;

@EnableKafka
@Configuration
public class KafkaConfig {
    @Value("${kafka.bootstrap-servers}")
    private String bootStrapServers;

    @Bean
    public ProducerFactory<String, BaseEvent> retryProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootStrapServers);
    }

    @Bean
    public KafkaTemplate<String, BaseEvent> retryKafkaTemplate() {
        return KafkaCommonConfigs.defaultKafkaTemplate(retryProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, BaseEvent> kakaoConsumerFactory() {
        return KafkaCommonConfigs.defaultConsumerFactory(bootStrapServers, KafkaGroupIds.KAKAO);
    }

    @Bean
    public ConsumerFactory<String, BaseEvent> emailConsumerFactory() {
        return KafkaCommonConfigs.defaultConsumerFactory(bootStrapServers, KafkaGroupIds.EMAIL);
    }

    @Bean
    public ConsumerFactory<String, BaseEvent> inAppConsumerFactory() {
        return KafkaCommonConfigs.defaultConsumerFactory(bootStrapServers, KafkaGroupIds.IN_APP);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BaseEvent> kakaoListenerContainerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(kakaoConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BaseEvent> emailListenerContainerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(emailConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BaseEvent> inAppListenerContainerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(inAppConsumerFactory());
    }
}
