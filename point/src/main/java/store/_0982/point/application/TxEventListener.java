package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.point.domain.event.*;
import store._0982.point.infrastructure.kafka.PointEventPublisher;

@Component
@RequiredArgsConstructor
public class TxEventListener {
    private final PointEventPublisher pointEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentConfirmed(PaymentConfirmedTxEvent event) {
        pointEventPublisher.publishPaymentConfirmedEvent(event.pgPayment());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointCharged(PointChargedTxEvent event) {
        pointEventPublisher.publishPointChargedEvent(event.history());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointDeducted(PointDeductedTxEvent event) {
        pointEventPublisher.publishPointDeductedEvent(event.history());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointReturned(PointReturnedTxEvent event) {
        pointEventPublisher.publishPointReturnedEvent(event.history());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointTransferred(PointTransferredTxEvent event) {
        pointEventPublisher.publishPointTransferredEvent(event.transaction());
    }
}
