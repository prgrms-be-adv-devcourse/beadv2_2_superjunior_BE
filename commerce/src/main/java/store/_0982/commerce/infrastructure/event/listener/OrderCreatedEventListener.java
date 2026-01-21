package store._0982.commerce.infrastructure.event.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.commerce.application.order.event.OrderCreateProcessedEvent;
import store._0982.commerce.infrastructure.kafka.publisher.OrderCreatedKafkaEventPublisher;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener {

    private final OrderCreatedKafkaEventPublisher orderCreatedKafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createOrder(OrderCreateProcessedEvent event){
        orderCreatedKafkaEventPublisher.publish(event);
    }
}
