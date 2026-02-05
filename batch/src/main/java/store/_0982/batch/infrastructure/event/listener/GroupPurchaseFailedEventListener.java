package store._0982.batch.infrastructure.event.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseChunkFailedEvent;
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseFailedProcessedEvent;
import store._0982.batch.infrastructure.kafka.publisher.GroupPurchaseFailedKafkaEventPublisher;
import store._0982.common.domain.grouppurchase.GroupPurchase;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupPurchaseFailedEventListener {

    private final GroupPurchaseFailedKafkaEventPublisher groupPurchaseFailedKafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupPurchasesFailed(GroupPurchaseChunkFailedEvent event){
        List<GroupPurchase> failedGroupPurchases = event.failedGroupPurchases();

        for(GroupPurchase groupPurchase : failedGroupPurchases){
            GroupPurchaseFailedProcessedEvent kafkaEvent = new GroupPurchaseFailedProcessedEvent(groupPurchase, "공동 구매 실패");
            groupPurchaseFailedKafkaEventPublisher.publish(kafkaEvent);
        }

    }
}
