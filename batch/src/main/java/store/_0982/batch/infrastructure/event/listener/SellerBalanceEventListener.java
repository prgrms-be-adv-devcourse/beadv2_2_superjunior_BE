package store._0982.batch.infrastructure.event.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.batch.application.sellerbalance.event.SellerBalanceCompleted;
import store._0982.batch.infrastructure.kafka.publisher.SellerBalanceEventPublisher;

@RequiredArgsConstructor
@Component
public class SellerBalanceEventListener {

    private final SellerBalanceEventPublisher sellerBalanceEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFailed(SellerBalanceCompleted event) {
        sellerBalanceEventPublisher.publishSellerBalanceCompletedEvent(event.sellerBalance(), event.amount());
    }
}
