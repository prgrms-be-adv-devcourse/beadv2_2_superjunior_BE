package store._0982.point.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import store._0982.point.application.TossPaymentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;

@SpringBootTest
@RecordApplicationEvents
@ContextConfiguration(initializers = {PostgreSQLContainerInitializer.class, KafkaContainerInitializer.class})
public abstract class BaseIntegrationTest {

    @Autowired
    protected ApplicationEvents events;

    @MockitoBean
    protected TossPaymentService tossPaymentService;

    @BeforeEach
    void baseSetUp() {
        reset(tossPaymentService);
    }

    protected void assertEventPublishedOnce(Class<?> clazz) {
        assertThat(events.stream(clazz).count()).isEqualTo(1);
    }
}
