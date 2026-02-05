package store._0982.point.support;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.testcontainers.kafka.KafkaContainer;

import java.util.Map;

public class KafkaContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:3.9.1");

    static {
        KAFKA.start();
    }

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext context) {
        Map<String, String> properties = Map.of(
                "spring.kafka.bootstrap-servers", KAFKA.getBootstrapServers()
        );
        TestPropertyValues.of(properties).applyTo(context);
    }
}
