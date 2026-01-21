package store._0982.batch.infrastructure.event.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultWithProductInfo;
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseChunkUpdateEvent;
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseUpdateProcessedEvent;
import store._0982.batch.infrastructure.kafka.publisher.GroupPurchaseUpdatedKafkaEventPublisher;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupPurchaseOpenEventListener {

    private final GroupPurchaseUpdatedKafkaEventPublisher groupPurchaseUpdatedKafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupPurchaseChunkUpdate(GroupPurchaseChunkUpdateEvent event) {
        List<GroupPurchaseResultWithProductInfo> updateList = event.updatedItems();

        for(GroupPurchaseResultWithProductInfo info : updateList){
            GroupPurchaseUpdateProcessedEvent kafkaEvent = new GroupPurchaseUpdateProcessedEvent(info);
            groupPurchaseUpdatedKafkaEventPublisher.publish(kafkaEvent);
        }
    }
}
