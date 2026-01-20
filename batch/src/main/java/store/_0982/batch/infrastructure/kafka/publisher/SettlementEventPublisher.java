package store._0982.batch.infrastructure.kafka.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import store._0982.batch.domain.settlement.Settlement;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.SettlementDoneEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementEventPublisher {

    private final KafkaTemplate<String, SettlementDoneEvent> settlementKafkaTemplate;

    public void publishSettlementCompletedEvent(Settlement settlement) {
        SettlementDoneEvent event = settlement.toCompletedEvent();
        send(settlement.getSettlementId().toString(), event);
    }

    public void publishSettlementFailedEvent(Settlement settlement) {
        SettlementDoneEvent event = settlement.toFailedEvent();
        send(settlement.getSettlementId().toString(), event);
    }

    public void publishSettlementDeferredEvent(Settlement settlement) {
        SettlementDoneEvent event = settlement.toDeferredEvent();
        send(settlement.getSettlementId().toString(), event);
    }

    private void send(String key, SettlementDoneEvent event) {
        settlementKafkaTemplate.send(KafkaTopics.SETTLEMENT_DONE, key, event)
                .whenComplete((result, throwable) -> {
                    if (throwable == null) {
                        log.info("[KAFKA] [{}] successfully sent to partition {}", KafkaTopics.SETTLEMENT_DONE, result.getRecordMetadata().partition());
                    } else {
                        log.error("[ERROR] [KAFKA] [{}] failed to send after infrastructure retries", KafkaTopics.SETTLEMENT_DONE, throwable);
                    }
                });
    }
}
