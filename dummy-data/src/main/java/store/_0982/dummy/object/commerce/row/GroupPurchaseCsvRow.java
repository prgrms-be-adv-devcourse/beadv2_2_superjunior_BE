package store._0982.dummy.object.commerce.row;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@JsonPropertyOrder({
        "groupPurchaseId",
        "minQuantity",
        "maxQuantity",
        "title",
        "description",
        "discountedPrice",
        "status",
        "startDate",
        "endDate",
        "sellerId",
        "productId",
        "currentQuantity",
        "likeCount",
        "createdAt",
        "updatedAt",
        "settledAt",
        "returnedAt",
        "succeededAt",
        "imageUrl"
})
public record GroupPurchaseCsvRow(
        UUID groupPurchaseId,
        int minQuantity,
        int maxQuantity,
        String title,
        String description,
        Long discountedPrice,
        GroupPurchaseStatus status,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        UUID sellerId,
        UUID productId,
        int currentQuantity,
        int likeCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime settledAt,
        OffsetDateTime returnedAt,
        OffsetDateTime succeededAt,
        String imageUrl
) {
}
