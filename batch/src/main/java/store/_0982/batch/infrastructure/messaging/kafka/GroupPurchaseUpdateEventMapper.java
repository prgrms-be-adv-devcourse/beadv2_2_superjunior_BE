package store._0982.batch.infrastructure.messaging.kafka;

import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultProjection;
import store._0982.common.kafka.dto.GroupPurchaseEvent;

public class GroupPurchaseUpdateEventMapper {

    public static GroupPurchaseEvent toMessage(GroupPurchaseResultProjection result) {
        return new GroupPurchaseEvent(
                result.groupPurchaseId(),
                result.sellerId(),
                result.title(),
                result.description(),
                result.discountedPrice(),
                result.productId(),
                GroupPurchaseEvent.Status.valueOf(result.targetStatus().name()),
                result.endDate().toString(),
                result.updatedAt() != null ? result.updatedAt().toString() : null,
                result.currentQuantity(),
                GroupPurchaseEvent.EventStatus.UPDATE_GROUP_PURCHASE,
                result.originalPrice(),
                GroupPurchaseEvent.ProductCategory.valueOf(result.productCategory().name())
        );
    }
}
