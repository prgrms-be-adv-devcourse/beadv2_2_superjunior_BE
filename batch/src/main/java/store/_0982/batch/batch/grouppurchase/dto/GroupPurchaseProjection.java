package store._0982.batch.batch.grouppurchase.dto;

import store._0982.batch.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.batch.domain.product.ProductCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * GroupPurchase와 Product에서 배치 처리에 필요한 필드만 조회하는 Projection DTO.
 * 엔티티 전체를 가져오지 않아 낙관적 락 충돌을 방지합니다.
 */
public record GroupPurchaseProjection(
        UUID groupPurchaseId,
        GroupPurchaseStatus status,
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
        ProductCategory productCategory
) {
}
