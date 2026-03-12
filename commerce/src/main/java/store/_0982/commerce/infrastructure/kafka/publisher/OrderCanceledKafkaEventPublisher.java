package store._0982.commerce.infrastructure.kafka.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store._0982.commerce.application.order.event.OrderCancelProcessedEvent;
import store._0982.commerce.infrastructure.outbox.OutboxEventService;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderCanceledEvent;

@Component
@RequiredArgsConstructor
public class OrderCanceledKafkaEventPublisher {

    private final OutboxEventService outboxEventService;

    public void publish(OrderCancelProcessedEvent event) {
        OrderCanceledEvent kafkaEvent = event.canceledOrder().toEvent(
                event.productName(),
                OrderCanceledEvent.PaymentMethod.valueOf(
                        event.canceledOrder().getPaymentMethod().name()
                )
        );

        outboxEventService.record(
                KafkaTopics.ORDER_CANCELED,
                kafkaEvent.getEventId().toString(),
                kafkaEvent,
                "Order",
                kafkaEvent.getOrderId().toString()
        );
    }
}
