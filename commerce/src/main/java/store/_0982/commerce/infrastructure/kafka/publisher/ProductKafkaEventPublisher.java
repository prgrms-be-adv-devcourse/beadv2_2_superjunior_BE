package store._0982.commerce.infrastructure.kafka.publisher;


import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import store._0982.commerce.application.order.event.OrderCancelProcessedEvent;
import store._0982.commerce.application.product.event.ProductUpsertedEvent;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderCanceledEvent;

@Component
@RequiredArgsConstructor
public class ProductKafkaEventPublisher {

    private final KafkaTemplate<String, store._0982.common.kafka.dto.ProductUpsertedEvent> kafkaTemplate;

    public void pulbish(ProductUpsertedEvent event) {
        store._0982.common.kafka.dto.ProductUpsertedEvent kafkaEvent = event.product().toEvent(event.product().getCategory());

        kafkaTemplate.send(
                KafkaTopics.PRODUCT_UPSERTED,
                kafkaEvent.getProductId().toString(),
                kafkaEvent
        );
    }
}

