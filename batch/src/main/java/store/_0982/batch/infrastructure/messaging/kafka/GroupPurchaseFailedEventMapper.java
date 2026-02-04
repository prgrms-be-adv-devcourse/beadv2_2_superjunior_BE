package store._0982.batch.infrastructure.messaging.kafka;

import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultProjection;
import store._0982.common.kafka.dto.GroupPurchaseFailedEvent;

public class GroupPurchaseFailedEventMapper {

    public static GroupPurchaseFailedEvent toMessage(GroupPurchaseResultProjection item) {
        return new GroupPurchaseFailedEvent(
                item.groupPurchaseId(),
                "최소 수량 미달"
        );
    }
}
