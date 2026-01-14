package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.commerce.application.order.dto.OrderCreatedEvent;
import store._0982.commerce.application.order.event.OrderCancelProcessedEvent;
import store._0982.commerce.infrastructure.client.payment.PaymentClient;
import store._0982.commerce.infrastructure.client.payment.dto.PointDeductRequest;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderCanceledEvent;

@Component
@RequiredArgsConstructor
public class OrderPointListener {
    private final PaymentClient paymentClient;

    private final KafkaTemplate<String, store._0982.common.kafka.dto.OrderCanceledEvent> orderCanceledKafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createOrder(OrderCreatedEvent event){
        paymentClient.deductPointsInternal(
                event.memberId(),
                new PointDeductRequest(
                        event.idempotencyKey(),
                        event.orderId(),
                        event.amount()
                )
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
