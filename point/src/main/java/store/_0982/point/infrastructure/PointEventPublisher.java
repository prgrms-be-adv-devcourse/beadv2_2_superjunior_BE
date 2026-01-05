package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PointEvent;
import store._0982.point.domain.entity.MemberPointHistory;
import store._0982.point.domain.entity.PaymentPoint;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointEventPublisher {
    private final KafkaTemplate<String, PointEvent> kafkaTemplate;

    public void publishPointRechargedEvent(PaymentPoint paymentPoint) {
        PointEvent event = createPointRechargedEvent(paymentPoint);
        send(KafkaTopics.POINT_RECHARGED, paymentPoint.getMemberId().toString(), event);
    }

    public void publishPointDeductedEvent(MemberPointHistory history) {
        PointEvent event = createPointChangedEvent(history, PointEvent.Status.DEDUCTED);
        send(KafkaTopics.POINT_CHANGED, history.getMemberId().toString(), event);
    }

    public void publishPointReturnedEvent(MemberPointHistory history) {
        PointEvent event = createPointChangedEvent(history, PointEvent.Status.RETURNED);
        send(KafkaTopics.POINT_CHANGED, history.getMemberId().toString(), event);
    }

    private static PointEvent createPointRechargedEvent(PaymentPoint paymentPoint) {
        return new PointEvent(
                paymentPoint.getId(),
                paymentPoint.getMemberId(),
                paymentPoint.getAmount(),
                PointEvent.Status.RECHARGED,
                paymentPoint.getPaymentMethod()
        );
    }

    private static PointEvent createPointChangedEvent(MemberPointHistory history, PointEvent.Status status) {
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
