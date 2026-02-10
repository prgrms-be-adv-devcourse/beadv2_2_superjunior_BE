package store._0982.commerce.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.RecordApplicationEvents;

@SpringBootTest
@RecordApplicationEvents
@ContextConfiguration(initializers = {PostgreSQLContainerInitializer.class})
public abstract class BaseIntegrationTest {

}

