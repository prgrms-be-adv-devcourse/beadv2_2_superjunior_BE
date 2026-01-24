package store._0982.point.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RecordApplicationEvents
@ContextConfiguration(initializers = {PostgreSQLContainerInitializer.class, KafkaContainerInitializer.class})
public abstract class BaseIntegrationTest {

    @Autowired
    protected ApplicationEvents events;

    protected void assertEventPublished(Class<?> clazz) {
        assertThat(events.stream(clazz).count()).isEqualTo(1);
    }
}
