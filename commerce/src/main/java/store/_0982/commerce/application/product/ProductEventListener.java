package store._0982.commerce.application.product;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.commerce.application.product.event.ProductCreatedEvent;
import store._0982.commerce.application.product.event.ProductDeletedEvent;
import store._0982.commerce.application.product.event.ProductUpdatedEvent;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductEvent;

@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductCreated(ProductCreatedEvent event) {
        ProductEvent kafkaEvent = event.product().toEvent();
        kafkaTemplate.send(
                KafkaTopics.PRODUCT_UPSERTED,
                kafkaEvent.getId().toString(),
                kafkaEvent
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUpdated(ProductUpdatedEvent event) {
        ProductEvent kafkaEvent = event.product().toEvent();
        kafkaTemplate.send(
                KafkaTopics.PRODUCT_UPSERTED,
                kafkaEvent.getId().toString(),
                kafkaEvent
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductDeleted(ProductDeletedEvent event) {
        ProductEvent kafkaEvent = event.product().toEvent();
        kafkaTemplate.send(
                KafkaTopics.PRODUCT_DELETED,
                kafkaEvent.getId().toString(),
                kafkaEvent
        );
    }
}
