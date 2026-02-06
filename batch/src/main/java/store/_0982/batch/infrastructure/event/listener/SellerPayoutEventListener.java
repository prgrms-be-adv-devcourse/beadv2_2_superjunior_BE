package store._0982.batch.infrastructure.event.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.batch.application.sellerpayout.event.SellerPayoutCompletedEvent;
import store._0982.batch.application.sellerpayout.event.SellerPayoutDeferredEvent;
import store._0982.batch.application.sellerpayout.event.SellerPayoutFailedEvent;
import store._0982.batch.infrastructure.kafka.publisher.SellerPayoutEventPublisher;

@Component
@RequiredArgsConstructor
public class SellerPayoutEventListener {

    private final SellerPayoutEventPublisher settlementEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCompleted(SellerPayoutCompletedEvent event) {
        settlementEventPublisher.publishSellerPayoutCompletedEvent(event.sellerPayout());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFailed(SellerPayoutFailedEvent event) {
        settlementEventPublisher.publishSellerPayoutFailedEvent(event.sellerPayout());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFailed(SellerPayoutDeferredEvent event) {
        settlementEventPublisher.publishSellerPayoutDeferredEvent(event.sellerPayout());
    }
}
