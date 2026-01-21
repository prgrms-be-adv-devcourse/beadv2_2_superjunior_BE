package store._0982.batch.infrastructure.messaging.kafka;

import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.batch.domain.product.Product;
import store._0982.common.kafka.dto.GroupPurchaseEvent;

public class GroupPurchaseUpdateEventMapper {

    public static GroupPurchaseEvent toMessage(GroupPurchase groupPurchase, Product product){
        return new GroupPurchaseEvent(
                groupPurchase.getGroupPurchaseId(),
                groupPurchase.getSellerId(),
                groupPurchase.getTitle(),
                groupPurchase.getDescription(),
                groupPurchase.getDiscountedPrice(),
                groupPurchase.getProductId(),
                GroupPurchaseEvent.Status.valueOf(groupPurchase.getStatus().toString()),
                groupPurchase.getEndDate().toString(),
                groupPurchase.getUpdatedAt().toString(),
                groupPurchase.getCurrentQuantity(),
                GroupPurchaseEvent.EventStatus.UPDATE_GROUP_PURCHASE,
                product.getPrice(),
                GroupPurchaseEvent.ProductCategory.valueOf(product.getCategory().toString())
        );
    }
}
