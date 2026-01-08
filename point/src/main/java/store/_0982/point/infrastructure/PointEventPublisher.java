package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PointEvent;
import store._0982.point.domain.entity.PointHistory;
import store._0982.point.domain.entity.Payment;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointEventPublisher {
    private final KafkaTemplate<String, PointEvent> kafkaTemplate;

    public void publishPointRechargedEvent(Payment payment) {
        PointEvent event = createPointRechargedEvent(payment);
        send(KafkaTopics.POINT_RECHARGED, payment.getMemberId().toString(), event);
    }

    public void publishPointDeductedEvent(PointHistory history) {
        PointEvent event = createPointChangedEvent(history, PointEvent.Status.DEDUCTED);
        send(KafkaTopics.POINT_CHANGED, history.getMemberId().toString(), event);
    }

    public void publishPointReturnedEvent(PointHistory history) {
        PointEvent event = createPointChangedEvent(history, PointEvent.Status.RETURNED);
        send(KafkaTopics.POINT_CHANGED, history.getMemberId().toString(), event);
    }

    private static PointEvent createPointRechargedEvent(Payment payment) {
        return new PointEvent(
                payment.getId(),
                payment.getMemberId(),
                payment.getAmount(),
                PointEvent.Status.RECHARGED,
                payment.getPaymentMethod()
        );
    }

    private static PointEvent createPointChangedEvent(PointHistory history, PointEvent.Status status) {
        return new PointEvent(
                history.getId(),
                history.getMemberId(),
                history.getAmount(),
                status,
                null
        );
    }

    private void send(String topic, String key, PointEvent event) {
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, throwable) -> {
                    if (throwable == null) {
                        log.info("[Kafka] {} successfully sent to partition {}", topic, result.getRecordMetadata().partition());
                    } else {
                        log.error("[Kafka] {} failed to send after infrastructure retries", topic, throwable);
                    }
                });
    }
}
