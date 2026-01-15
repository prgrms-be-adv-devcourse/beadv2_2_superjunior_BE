package store._0982.member.config.member;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ProducerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.dto.MemberDeletedEvent;

@EnableKafka
@Configuration
public class MemberKafkaConfig {
    @Value("${kafka.bootstrap-servers}")
    private String bootStrapServers;

    @Bean
    public ProducerFactory<String, MemberDeletedEvent> memberDeletedProducerFactory() {
        return KafkaCommonConfigs.defaultProducerFactory(bootStrapServers);
    }
}
