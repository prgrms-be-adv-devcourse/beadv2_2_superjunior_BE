package store._0982.batch.application.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.batch.application.settlement.event.SettlementProcessedEvent;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.SettlementDoneEvent;

@Component
@RequiredArgsConstructor
public class SettlementListener {

    private final KafkaTemplate<String, SettlementDoneEvent> settlementKafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishCompleted(SettlementProcessedEvent event) {
        SettlementDoneEvent kafkaEvent = event.settlement().toEvent(
                SettlementDoneEvent.Status.valueOf(
                        event.settlement().getStatus().name()
                )
        );
        settlementKafkaTemplate.send(
                KafkaTopics.SETTLEMENT_DONE,
                kafkaEvent.getId().toString(),
                kafkaEvent
        );
    }
}
