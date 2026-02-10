package store._0982.recommendation.infrastructure.feign.commerce.dto;

import store._0982.common.domain.grouppurchase.GroupPurchase;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OpenGroupPurchaseInfo(
        UUID groupPurchaseId,
        UUID productId,
        Long discountedPrice,
        OffsetDateTime endDate
) {
    public static OpenGroupPurchaseInfo from(GroupPurchase groupPurchase) {
        return new OpenGroupPurchaseInfo(
                groupPurchase.getGroupPurchaseId(),
                groupPurchase.getProductId(),
                groupPurchase.getDiscountedPrice(),
                groupPurchase.getEndDate()
        );
    }
}
