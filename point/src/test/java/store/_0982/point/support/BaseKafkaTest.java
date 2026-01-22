package store._0982.point.support;

import org.awaitility.core.ThrowingRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import store._0982.common.kafka.dto.BaseEvent;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public abstract class BaseKafkaTest extends BaseIntegrationTest {

    private static final int DEFAULT_TIMEOUT = 5;

    @Autowired
    protected KafkaTemplate<String, BaseEvent> kafkaTemplate;

    protected void awaitUntilAsserted(ThrowingRunnable throwingRunnable) {
        await().atMost(DEFAULT_TIMEOUT, TimeUnit.SECONDS).untilAsserted(throwingRunnable);
    }

}
