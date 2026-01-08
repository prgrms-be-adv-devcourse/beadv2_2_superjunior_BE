package store._0982.elasticsearch.domain.reindex;

import store._0982.elasticsearch.infrastructure.reindex.GroupPurchaseReindexProjection;

import java.time.Instant;
import java.util.UUID;

public record GroupPurchaseReindexRow(
        UUID groupPurchaseId,
        String title,
        String description,
        String status,
        Instant updatedAt,
        String category,
        UUID sellerId
) {
    public static GroupPurchaseReindexRow from(GroupPurchaseReindexProjection projection) {
        return new GroupPurchaseReindexRow(
                projection.getGroupPurchaseId(),
                projection.getTitle(),
                projection.getDescription(),
                projection.getStatus(),
                projection.getUpdatedAt(),
                projection.getCategory(),
                projection.getSellerId()
        );
    }
}
