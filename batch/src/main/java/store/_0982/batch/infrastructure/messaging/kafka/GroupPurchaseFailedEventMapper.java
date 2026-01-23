package store._0982.batch.infrastructure.messaging.kafka;

import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.common.kafka.dto.GroupPurchaseFailedEvent;

public class GroupPurchaseFailedEventMapper {

    public static GroupPurchaseFailedEvent toMessage(GroupPurchase groupPurchase, String reason){
        return new GroupPurchaseFailedEvent(
                groupPurchase.getGroupPurchaseId(),
                reason
        );
    }
}
