package store._0982.commerce.infrastructure.kafka.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store._0982.commerce.application.product.event.ProductUpsertedEvent;
import store._0982.commerce.infrastructure.outbox.OutboxEventService;
import store._0982.common.kafka.KafkaTopics;

@Component
@RequiredArgsConstructor
public class ProductKafkaEventPublisher {

    private final OutboxEventService outboxEventService;

    public void pulbish(ProductUpsertedEvent event) {
        store._0982.common.kafka.dto.ProductUpsertedEvent kafkaEvent = event.product().toEvent(event.product().getCategory());

        outboxEventService.record(
                KafkaTopics.PRODUCT_UPSERTED,
                kafkaEvent.getEventId().toString(),
                kafkaEvent,
                "Product",
                kafkaEvent.getProductId().toString()
        );
    }
}
