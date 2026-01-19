package store._0982.commerce.infrastructure.event.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.commerce.application.order.event.OrderCancelProcessedEvent;
import store._0982.commerce.infrastructure.kafka.publisher.OrderCanceledKafkaEventPublisher;

@Component
@RequiredArgsConstructor
public class OrderCanceledEventListener {

    private final OrderCanceledKafkaEventPublisher orderCanceledKafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void cancelOrder(OrderCancelProcessedEvent event) {
        orderCanceledKafkaEventPublisher.publish(event);
    }
}
