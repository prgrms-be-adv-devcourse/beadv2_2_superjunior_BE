package store._0982.point.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PointEvent;
import store._0982.point.domain.entity.Payment;
import store._0982.point.domain.entity.PointHistory;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointEventPublisher {
    private final KafkaTemplate<String, PointEvent> kafkaTemplate;


    public void publishPaymentConfirmedEvent(Payment payment) {
        // TODO: 결제 승인 이벤트 발송 추가
    }

    public void publishPointChargedEvent(PointHistory history) {
        PointEvent event = createPointChangedEvent(history, PointEvent.Status.RECHARGED);
        send(KafkaTopics.POINT_RECHARGED, history.getMemberId().toString(), event);
    }

    public void publishPointDeductedEvent(PointHistory history) {
        PointEvent event = createPointChangedEvent(history, PointEvent.Status.DEDUCTED);
        send(KafkaTopics.POINT_CHANGED, history.getMemberId().toString(), event);
    }

    public void publishPointReturnedEvent(PointHistory history) {
        PointEvent event = createPointChangedEvent(history, PointEvent.Status.RETURNED);
        send(KafkaTopics.POINT_CHANGED, history.getMemberId().toString(), event);
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
