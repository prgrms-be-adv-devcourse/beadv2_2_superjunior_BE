package store._0982.elasticsearch.application.dto;

import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.elasticsearch.domain.GroupPurchaseDocument;
import store._0982.elasticsearch.domain.ProductDocumentEmbedded;

import java.time.OffsetDateTime;

public record GroupPurchaseDocumentCommand(
        String groupPurchaseId,
        String title,
        String description,
        String status,
        Long discountedPrice,
        Integer currentQuantity,
        OffsetDateTime endDate,
        OffsetDateTime updatedAt,
        String sellerId,
        String productId,
        String productCategory,
        Long originalPrice,
        float[] productVector
) {
    public static GroupPurchaseDocumentCommand from(GroupPurchaseEvent event) {
        return from(event, null);
    }

    public static GroupPurchaseDocumentCommand from(GroupPurchaseEvent event, float[] productVector) {
        String productId = null;
        if (event.getProductId() != null) {
            productId = event.getProductId().toString();
        }
        return new GroupPurchaseDocumentCommand(
                event.getId().toString(),
                event.getTitle(),
                event.getDescription(),
                event.getGroupPurchaseStatus() != null ? event.getGroupPurchaseStatus().toString() : null,
                event.getDiscountedPrice(),
                event.getCurrentQuantity(),
                event.getEndDate() != null ? OffsetDateTime.parse(event.getEndDate()) : null,
                event.getUpdatedAt() != null ? OffsetDateTime.parse(event.getUpdatedAt()) : null,
                event.getSellerId() != null ? event.getSellerId().toString() : null,
                productId,
                event.getProductCategory() != null ? event.getProductCategory().toString() : null,
                event.getOriginalPrice(),
                productVector
        );
    }

    public GroupPurchaseDocument toDocument() {
        return GroupPurchaseDocument.builder()
                .groupPurchaseId(groupPurchaseId)
                .title(title)
                .description(description)
                .status(status)
                .discountedPrice(discountedPrice)
                .currentQuantity(currentQuantity)
                .endDate(endDate)
                .updatedAt(updatedAt)
                .discountRate(calculateDiscountRate(originalPrice, discountedPrice))
                .productVector(productVector)
                .productDocumentEmbedded(new ProductDocumentEmbedded(
                        productId,
                        productCategory,
                        originalPrice,
                        sellerId
                ))
                .build();
    }

    private static Long calculateDiscountRate(Long originalPrice, Long discountedPrice) {
        if (originalPrice == null || discountedPrice == null) {
            return 0L;
        }
        if (originalPrice <= 0 || discountedPrice >= originalPrice) {
            return 0L;
        }

        return Math.round(
                ((double) (originalPrice - discountedPrice) / originalPrice) * 100
        );
    }
}
