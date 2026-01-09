package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.point.domain.event.PointDeductedEvent;
import store._0982.point.domain.event.PointRechargedEvent;
import store._0982.point.domain.event.PointReturnedEvent;
import store._0982.point.infrastructure.kafka.PointEventPublisher;

@Component
@RequiredArgsConstructor
public class PointEventListener {
    private final PointEventPublisher pointEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointRecharged(PointRechargedEvent event) {
        pointEventPublisher.publishPointRechargedEvent(event.payment());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointDeducted(PointDeductedEvent event) {
        pointEventPublisher.publishPointDeductedEvent(event.history());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointReturned(PointReturnedEvent event) {
        pointEventPublisher.publishPointReturnedEvent(event.history());
    }
}
