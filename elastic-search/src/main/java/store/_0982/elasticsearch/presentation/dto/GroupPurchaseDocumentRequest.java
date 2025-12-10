package store._0982.elasticsearch.presentation.dto;

import store._0982.elasticsearch.application.dto.GroupPurchaseDocumentCommand;

import java.time.Instant;

public record GroupPurchaseDocumentRequest(
        String groupPurchaseId,
        String productId,
        String sellerName,
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

    public GroupPurchaseDocumentCommand toCommand() {
        return new GroupPurchaseDocumentCommand(
                groupPurchaseId,
                productId,
                sellerName,
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
