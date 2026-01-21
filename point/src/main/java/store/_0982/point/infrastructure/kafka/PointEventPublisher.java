package store._0982.point.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PointChangedEvent;
import store._0982.point.domain.entity.PointTransaction;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointEventPublisher {

    private final KafkaTemplate<String, PointChangedEvent> kafkaTemplate;

    public void publishPointChargedEvent(PointTransaction history) {
        PointChangedEvent event = createPointChangedEvent(history, PointChangedEvent.Status.CHARGED);
        send(history.getMemberId().toString(), event);
    }

    public void publishPointDeductedEvent(PointTransaction history) {
        PointChangedEvent event = createPointChangedEvent(history, PointChangedEvent.Status.DEDUCTED);
        send(history.getMemberId().toString(), event);
    }

    public void publishPointReturnedEvent(PointTransaction history) {
        PointChangedEvent event = createPointChangedEvent(history, PointChangedEvent.Status.RETURNED);
        send(history.getMemberId().toString(), event);
    }

    // TODO: Kafka 이벤트 객체에 '출금됨' 상태를 추가할까?
    public void publishPointTransferredEvent(PointTransaction history) {
        PointChangedEvent event = createPointChangedEvent(history, PointChangedEvent.Status.DEDUCTED);
        send(history.getMemberId().toString(), event);
    }

    private static PointChangedEvent createPointChangedEvent(PointTransaction history, PointChangedEvent.Status status) {
        return new PointChangedEvent(
                history.getOrderId(),
                history.getMemberId(),
                history.getTotalAmount(),
                status,
                null
        );
    }

    private void send(String key, PointChangedEvent event) {
        kafkaTemplate.send(KafkaTopics.POINT_CHANGED, key, event)
                .whenComplete((result, throwable) -> {
                    if (throwable == null) {
                        log.info("[Kafka] {} successfully sent to partition {}", KafkaTopics.POINT_CHANGED, result.getRecordMetadata().partition());
                    } else {
                        log.error("[Kafka] {} failed to send after infrastructure retries", KafkaTopics.POINT_CHANGED, throwable);
                    }
                });
    }
}
