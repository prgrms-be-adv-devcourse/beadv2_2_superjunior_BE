package store._0982.commerce.infrastructure.event.listener;


import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.commerce.application.product.event.ProductCreatedEvent;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductUpsertedEvent;

@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final KafkaTemplate<String, ProductUpsertedEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreated(ProductCreatedEvent event) {
        ProductUpsertedEvent kafkaEvent = event.product().toEvent(event.product().getCategory());

        kafkaTemplate.send(
                KafkaTopics.PRODUCT_UPSERTED,
                kafkaEvent.getProductId().toString(),
                kafkaEvent
        );
    }
}
