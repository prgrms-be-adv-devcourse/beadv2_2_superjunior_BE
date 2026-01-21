package store._0982.commerce.infrastructure.kafka.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import store._0982.commerce.application.order.event.OrderCancelProcessedEvent;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderCanceledEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCanceledKafkaEventPublisher {

    private final KafkaTemplate<String, OrderCanceledEvent> orderCanceledKafkaTemplate;

    public void publish(OrderCancelProcessedEvent event) {
        OrderCanceledEvent kafkaEvent = event.order().toEvent(
                event.reason(),
                OrderCanceledEvent.PaymentMethod.valueOf(
                        event.order().getPaymentMethod().name()
                ),
                event.amount()
        );
        send(kafkaEvent.getEventId().toString(), kafkaEvent);
    }

    private void send(String key, OrderCanceledEvent event) {
        orderCanceledKafkaTemplate.send(KafkaTopics.ORDER_CANCELED, key, event)
                .whenComplete((result, throwable) -> {
                    if (throwable == null) {
                        log.info("[KAFKA] [{}] successfully sent to partition {}", KafkaTopics.ORDER_CANCELED, result.getRecordMetadata().partition());
                    } else {
                        log.error("[ERROR] [KAFKA] [{}] failed to send after retries", KafkaTopics.ORDER_CANCELED, throwable);
                    }
                });
    }
}
