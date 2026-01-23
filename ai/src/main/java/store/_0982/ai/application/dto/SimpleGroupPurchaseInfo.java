package store._0982.ai.application.dto;

import java.util.UUID;

public record SimpleGroupPurchaseInfo(
        UUID groupPurchaseId,
        String title,
        String description,
        String productId,
        String category
) {
    public static SimpleGroupPurchaseInfo from(GroupPurchase groupPurchase) {
        return new SimpleGroupPurchaseInfo(
                groupPurchase.groupPurchaseId(),
                groupPurchase.title(),
                groupPurchase.description(),
                groupPurchase.productSearchInfo().productId(),
                groupPurchase.productSearchInfo().category()
        );
    }
}
