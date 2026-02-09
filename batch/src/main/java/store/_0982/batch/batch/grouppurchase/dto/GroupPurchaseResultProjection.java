package store._0982.batch.batch.grouppurchase.dto;

import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.common.domain.product.ProductCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Processor 결과물 - 상태 변경 정보 포함
 */
public record GroupPurchaseResultProjection(
        UUID groupPurchaseId,
        GroupPurchaseStatus targetStatus,
        int currentQuantity,
        int minQuantity,
        UUID sellerId,
        UUID productId,
        String title,
        String description,
        Long discountedPrice,
        OffsetDateTime endDate,
        OffsetDateTime updatedAt,
        Long originalPrice,
        ProductCategory productCategory,
        boolean success
) {
    public static GroupPurchaseResultProjection from(GroupPurchaseProjection projection,
                                                      GroupPurchaseStatus targetStatus,
                                                      boolean success) {
        return new GroupPurchaseResultProjection(
                projection.groupPurchaseId(),
                targetStatus,
                projection.currentQuantity(),
                projection.minQuantity(),
                projection.sellerId(),
                projection.productId(),
                projection.title(),
                projection.description(),
                projection.discountedPrice(),
                projection.endDate(),
                projection.updatedAt(),
                projection.originalPrice(),
                projection.productCategory(),
                success
        );
    }
}
