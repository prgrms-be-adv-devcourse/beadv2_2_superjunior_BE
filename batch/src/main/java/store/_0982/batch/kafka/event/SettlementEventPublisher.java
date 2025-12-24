package store._0982.batch.kafka.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.settlement.Settlement;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.SettlementEvent;

@Component
@RequiredArgsConstructor
public class SettlementEventPublisher {

    private final KafkaTemplate<String, SettlementEvent> settlementKafkaTemplate;

    public void publishCompleted(Settlement settlement) {
        SettlementEvent event = settlement.toCompletedEvent();
        settlementKafkaTemplate.send(
                KafkaTopics.MONTHLY_SETTLEMENT_COMPLETED,
                settlement.getSellerId().toString(),
                event
        );
    }

    public void publishFailed(Settlement settlement) {
        SettlementEvent event = settlement.toFailedEvent();
        settlementKafkaTemplate.send(
                KafkaTopics.MONTHLY_SETTLEMENT_FAILED,
                settlement.getSellerId().toString(),
                event
        );
    }

    public void publishDeferred(Settlement settlement) {
        SettlementEvent event = settlement.toDeferredEvent();
        settlementKafkaTemplate.send(
                KafkaTopics.MONTHLY_SETTLEMENT_COMPLETED,
                settlement.getSellerId().toString(),
                event
        );
    }

}
