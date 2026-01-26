package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.point.domain.event.*;
import store._0982.point.infrastructure.kafka.PaymentEventPublisher;
import store._0982.point.infrastructure.kafka.PointEventPublisher;

@Component
@RequiredArgsConstructor
public class TxEventListener {

    private final PaymentEventPublisher paymentEventPublisher;
    private final PointEventPublisher pointEventPublisher;

    // ============= PG 결제에 대한 이벤트 발송 =============
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentConfirmed(PaymentConfirmedTxEvent event) {
        paymentEventPublisher.publishPaymentConfirmedEvent(event.pgPayment());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentFailed(PaymentFailedTxEvent event) {
        paymentEventPublisher.publishPaymentFailedEvent(event.pgPayment());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCanceled(PaymentCanceledTxEvent event) {
        paymentEventPublisher.publishPaymentCanceledEvent(event.pgPayment());
    }

    // ============= 포인트 결제에 대한 이벤트 발송 =============
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
