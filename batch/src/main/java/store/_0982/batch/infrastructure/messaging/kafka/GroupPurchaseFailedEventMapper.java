package store._0982.batch.infrastructure.messaging.kafka;

import org.springframework.stereotype.Component;
import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.common.kafka.dto.GroupPurchaseFailedEvent;

@Component
public class GroupPurchaseFailedEventMapper {

    public GroupPurchaseFailedEvent toMessage(GroupPurchase groupPurchase, String reason){
        return new GroupPurchaseFailedEvent(
                groupPurchase.getGroupPurchaseId(),
                reason
        );
    }
}
