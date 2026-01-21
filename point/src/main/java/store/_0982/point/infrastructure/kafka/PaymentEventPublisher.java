package store._0982.point.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.BaseEvent;
import store._0982.common.kafka.dto.PaymentChangedEvent;
import store._0982.point.domain.entity.PgPayment;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;

    public void publishPaymentConfirmedEvent(PgPayment pgPayment) {
        PaymentChangedEvent event = createPointChangedEvent(pgPayment, PaymentChangedEvent.Status.COMPLETED);
        send(pgPayment.getMemberId().toString(), event);
    }

    public void publishPaymentFailedEvent(PgPayment pgPayment) {
        PaymentChangedEvent event = createPointChangedEvent(pgPayment, PaymentChangedEvent.Status.FAILED);
        send(pgPayment.getMemberId().toString(), event);
    }

    public void publishPaymentCanceledEvent(PgPayment pgPayment) {
        PaymentChangedEvent event = createPointChangedEvent(pgPayment, PaymentChangedEvent.Status.REFUNDED);
        send(pgPayment.getMemberId().toString(), event);
    }

    private static PaymentChangedEvent createPointChangedEvent(PgPayment pgPayment, PaymentChangedEvent.Status status) {
        return new PaymentChangedEvent(
                pgPayment.getMemberId(),
                pgPayment.getOrderId(),
                status
        );
    }

    private void send(String key, PaymentChangedEvent event) {
        kafkaTemplate.send(KafkaTopics.PAYMENT_CHANGED, key, event)
                .whenComplete((result, throwable) -> {
                    if (throwable == null) {
                        log.info("[Kafka] {} successfully sent to partition {}", KafkaTopics.POINT_CHANGED, result.getRecordMetadata().partition());
                    } else {
                        log.error("[Kafka] {} failed to send after infrastructure retries", KafkaTopics.POINT_CHANGED, throwable);
                    }
                });
    }
}
