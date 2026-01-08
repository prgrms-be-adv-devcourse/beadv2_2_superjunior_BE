package store._0982.elasticsearch.application.dto;

import store._0982.elasticsearch.domain.GroupPurchaseDocument;
import store._0982.elasticsearch.domain.ProductDocumentEmbedded;

import java.time.OffsetDateTime;

public record GroupPurchaseDocumentInfo(
        String groupPurchaseId,
        String title,
        String description,
        String status,
        Long discountedPrice,
        OffsetDateTime endDate,
        OffsetDateTime updatedAt,
        Long discountRate,
        ProductDocumentEmbedded productDocumentEmbedded
) {
    public static GroupPurchaseDocumentInfo from(GroupPurchaseDocument groupPurchaseDocument) {
        return new GroupPurchaseDocumentInfo(
                groupPurchaseDocument.getGroupPurchaseId(),
                groupPurchaseDocument.getTitle(),
                groupPurchaseDocument.getDescription(),
                groupPurchaseDocument.getStatus(),
                groupPurchaseDocument.getDiscountedPrice(),
                groupPurchaseDocument.getEndDate(),
                groupPurchaseDocument.getUpdatedAt(),
                groupPurchaseDocument.getDiscountRate(),
                groupPurchaseDocument.getProductDocumentEmbedded()
        );
    }
}

