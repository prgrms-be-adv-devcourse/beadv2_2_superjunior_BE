package store._0982.batch.infrastructure.event.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.batch.application.settlement.event.SettlementCompletedEvent;
import store._0982.batch.application.settlement.event.SettlementFailedEvent;
import store._0982.batch.infrastructure.kafka.publisher.SettlementEventPublisher;

@Component
@RequiredArgsConstructor
public class SettlementEventListener {

    private final SettlementEventPublisher settlementEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCompleted(SettlementCompletedEvent event) {
        settlementEventPublisher.publishSettlementCompletedEvent(event.settlement());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFailed(SettlementFailedEvent event) {
        settlementEventPublisher.publishSettlementFailedEvent(event.settlement());
    }
}
