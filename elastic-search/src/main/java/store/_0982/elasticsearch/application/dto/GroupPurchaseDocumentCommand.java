package store._0982.elasticsearch.application.dto;

import store._0982.elasticsearch.domain.GroupPurchaseDocument;

import java.time.Instant;

public record GroupPurchaseDocumentCommand(
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
    public GroupPurchaseDocument toDocument() {
        return GroupPurchaseDocument.builder()
                .groupPurchaseId(groupPurchaseId)
                .productId(productId)
                .sellerName(sellerName)
                .minQuantity(minQuantity)
                .maxQuantity(maxQuantity)
                .title(title)
                .description(description)
                .discountedPrice(discountedPrice)
                .status(status)
                .startAt(startAt)
                .endAt(endAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .participants(participants)
                .build();
    }
}
