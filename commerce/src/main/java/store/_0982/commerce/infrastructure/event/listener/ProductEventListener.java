package store._0982.commerce.infrastructure.event.listener;


import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.commerce.application.product.event.ProductUpsertedEvent;
import store._0982.common.kafka.KafkaTopics;

@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final KafkaTemplate<String, store._0982.common.kafka.dto.ProductUpsertedEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUpserted(ProductUpsertedEvent event) {
        store._0982.common.kafka.dto.ProductUpsertedEvent kafkaEvent = event.product().toEvent(event.product().getCategory());

        kafkaTemplate.send(
                KafkaTopics.PRODUCT_UPSERTED,
                kafkaEvent.getProductId().toString(),
                kafkaEvent
        );
    }
}
