package store._0982.batch.infrastructure.event.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultProjection;
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseChunkUpdateEvent;
import store._0982.batch.infrastructure.kafka.publisher.GroupPurchaseUpdatedKafkaEventPublisher;

@Component
@RequiredArgsConstructor
public class GroupPurchaseUpdateEventListener {

    private final GroupPurchaseUpdatedKafkaEventPublisher groupPurchaseUpdatedKafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupPurchaseChunkUpdate(GroupPurchaseChunkUpdateEvent event) {
        for (GroupPurchaseResultProjection item : event.updatedItems()) {
            groupPurchaseUpdatedKafkaEventPublisher.publish(item);
        }
    }
}
