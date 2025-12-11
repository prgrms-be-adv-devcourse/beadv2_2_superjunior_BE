package store._0982.elasticsearch.application.dto;

import store._0982.elasticsearch.domain.GroupPurchaseDocument;

import java.time.Instant;

public record GroupPurchaseDocumentInfo(
        String groupPurchaseId,
        String productId,
        String sellerName,
        Integer minQuantity,
        Integer maxQuantity,
        String title,
        String description,
        Long discountedPrice,
        String status,
        Instant startAt,
        Instant endAt,
        Instant createdAt,
        Instant updatedAt,
        Integer participants
) {
    public static GroupPurchaseDocumentInfo from(GroupPurchaseDocument groupPurchaseDocument) {
        return new GroupPurchaseDocumentInfo(
                groupPurchaseDocument.getGroupPurchaseId(),
                groupPurchaseDocument.getProductId(),
                groupPurchaseDocument.getSellerName(),
                groupPurchaseDocument.getMinQuantity(),
                groupPurchaseDocument.getMaxQuantity(),
                groupPurchaseDocument.getTitle(),
                groupPurchaseDocument.getDescription(),
                groupPurchaseDocument.getDiscountedPrice(),
                groupPurchaseDocument.getStatus(),
                groupPurchaseDocument.getStartAt(),
                groupPurchaseDocument.getEndAt(),
                groupPurchaseDocument.getCreatedAt(),
                groupPurchaseDocument.getUpdatedAt(),
                groupPurchaseDocument.getParticipants()
        );
    }
}
