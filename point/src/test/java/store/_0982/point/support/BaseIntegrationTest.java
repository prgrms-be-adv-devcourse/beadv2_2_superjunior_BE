package store._0982.point.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@EmbeddedKafka
@ContextConfiguration(initializers = {PostgreSQLContainerInitializer.class})
public abstract class BaseIntegrationTest {
}
