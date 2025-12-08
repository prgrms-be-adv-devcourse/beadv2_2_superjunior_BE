package store._0982.elasticsearch.purchase.presentation.dto;

import store._0982.elasticsearch.purchase.domain.GroupPurchaseDocument;


import java.time.Instant;

public record GroupPurchaseIndexRequest(
        String groupPurchaseId,
        String productId,
        Integer minQuantity,
        Integer maxQuantity,
        String title,
        String description,
        Integer discountedPrice,
        String status,
        Instant startAt,
        Instant endAt,
        Instant createdAt,
        Instant updatedAt,
        Integer participants
) {

    public GroupPurchaseDocument toDocument() {
        return new GroupPurchaseDocument(
                groupPurchaseId,
                productId,
                minQuantity,
                maxQuantity,
                title,
                description,
                discountedPrice,
                status,
                startAt,
                endAt,
                createdAt,
                updatedAt,
                participants
        );
    }
}
