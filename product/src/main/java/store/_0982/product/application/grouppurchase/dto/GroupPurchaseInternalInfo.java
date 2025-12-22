package store._0982.product.application.grouppurchase.dto;

import store._0982.product.domain.grouppurchase.GroupPurchase;

import java.util.UUID;

public record GroupPurchaseInternalInfo (
        UUID groupPurchaseId,
        UUID sellerId,
        Long totalAmount
){
    public static GroupPurchaseInternalInfo from(GroupPurchase groupPurchase) {
        return new GroupPurchaseInternalInfo(groupPurchase.getGroupPurchaseId(), groupPurchase.getSellerId(), ((long) groupPurchase.getCurrentQuantity() * groupPurchase.getDiscountedPrice()));
    }

}
