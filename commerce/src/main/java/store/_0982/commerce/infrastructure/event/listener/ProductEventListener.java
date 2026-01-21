package store._0982.commerce.infrastructure.event.listener;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.commerce.application.product.event.ProductUpsertedEvent;
import store._0982.commerce.infrastructure.kafka.publisher.ProductKafkaEventPublisher;

@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductKafkaEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUpserted(ProductUpsertedEvent event) {
        eventPublisher.pulbish(event);
    }
}
