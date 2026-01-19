package store._0982.commerce.infrastructure.event.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.commerce.application.order.event.OrderCancelProcessedEvent;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderCanceledEvent;

@Component
@RequiredArgsConstructor
public class OrderKafkaEventListener {

    private final KafkaTemplate<String, OrderCanceledEvent> orderCanceledKafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void cancelOrder(OrderCancelProcessedEvent event) {
        OrderCanceledEvent kafkaEvent = event.order().toEvent(
                event.reason(),
                OrderCanceledEvent.PaymentMethod.valueOf(
                        event.order().getPaymentMethod().name()
                ),
                event.amount()
        );
        orderCanceledKafkaTemplate.send(
                KafkaTopics.ORDER_CANCELED,
                kafkaEvent.getEventId().toString(),
                kafkaEvent
        );
    }
}
