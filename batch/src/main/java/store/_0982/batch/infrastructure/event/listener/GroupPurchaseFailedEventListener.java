package store._0982.batch.infrastructure.event.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultProjection;
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseChunkFailedEvent;
import store._0982.batch.infrastructure.kafka.publisher.GroupPurchaseFailedKafkaEventPublisher;

@Component
@RequiredArgsConstructor
public class GroupPurchaseFailedEventListener {

    private final GroupPurchaseFailedKafkaEventPublisher groupPurchaseFailedKafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupPurchasesFailed(GroupPurchaseChunkFailedEvent event) {
        for (GroupPurchaseResultProjection item : event.failedGroupPurchases()) {
            groupPurchaseFailedKafkaEventPublisher.publish(item);
        }
    }
}
