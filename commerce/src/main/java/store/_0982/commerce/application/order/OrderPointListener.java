package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.commerce.application.order.event.OrderCancelProcessedEvent;
import store._0982.commerce.application.order.event.OrderCreateProcessedEvent;
import store._0982.commerce.infrastructure.messaging.kafka.OrderCreatedEventMapper;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderCanceledEvent;
import store._0982.common.kafka.dto.OrderCreatedEvent;

@Component
@RequiredArgsConstructor
public class OrderPointListener {

    private final KafkaTemplate<String, OrderCreatedEvent> orderCreatedEventkafkaTemplate;
    private final KafkaTemplate<String, OrderCanceledEvent> orderCanceledKafkaTemplate;
    private final OrderCreatedEventMapper orderCreatedEventMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createOrder(OrderCreateProcessedEvent event){
        OrderCreatedEvent kafkaEvent = orderCreatedEventMapper.toMessage(
                event.order(),
                event.productName()
        );
        orderCreatedEventkafkaTemplate.send(
                KafkaTopics.ORDER_CREATED,
                kafkaEvent.getEventId().toString(),
                kafkaEvent
        );
    }



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
