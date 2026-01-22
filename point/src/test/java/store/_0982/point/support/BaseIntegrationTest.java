package store._0982.point.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ContextConfiguration;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.dto.BaseEvent;

@SpringBootTest
@EmbeddedKafka(kraft = true)
@ContextConfiguration(initializers = {PostgreSQLContainerInitializer.class})
public abstract class BaseIntegrationTest {

    @TestConfiguration
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    static class Config {

        @Autowired
        private EmbeddedKafkaKraftBroker embeddedKafkaBroker;

        @Bean({"testKafkaTemplate", "defaultRetryTopicKafkaTemplate"})
        public KafkaTemplate<String, BaseEvent> testKafkaTemplate() {
            ProducerFactory<String, BaseEvent> producerFactory =
                    KafkaCommonConfigs.defaultProducerFactory(embeddedKafkaBroker.getBrokersAsString());
            return KafkaCommonConfigs.defaultKafkaTemplate(producerFactory);
        }

    }

}
