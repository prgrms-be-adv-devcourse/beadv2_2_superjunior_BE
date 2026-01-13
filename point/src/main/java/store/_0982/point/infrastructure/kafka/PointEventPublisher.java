package store._0982.point.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PointChangedEvent;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.entity.PointPayment;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointEventPublisher {
    private final KafkaTemplate<String, PointChangedEvent> kafkaTemplate;


    public void publishPaymentConfirmedEvent(PgPayment pgPayment) {
        // TODO: 결제 승인 이벤트 발송 추가
    }

    public void publishPointChargedEvent(PointPayment history) {
        PointChangedEvent event = createPointChangedEvent(history, PointChangedEvent.Status.CHARGED);
        send(KafkaTopics.POINT_RECHARGED, history.getMemberId().toString(), event);
    }

    public void publishPointDeductedEvent(PointPayment history) {
        PointChangedEvent event = createPointChangedEvent(history, PointChangedEvent.Status.DEDUCTED);
        send(KafkaTopics.POINT_CHANGED, history.getMemberId().toString(), event);
    }

    public void publishPointReturnedEvent(PointPayment history) {
        PointChangedEvent event = createPointChangedEvent(history, PointChangedEvent.Status.RETURNED);
        send(KafkaTopics.POINT_CHANGED, history.getMemberId().toString(), event);
    }

    private static PointChangedEvent createPointChangedEvent(PointPayment history, PointChangedEvent.Status status) {
        return new PointChangedEvent(
                history.getId(),
                history.getMemberId(),
                history.getAmount(),
                status,
                null
        );
    }

    private void send(String topic, String key, PointChangedEvent event) {
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
