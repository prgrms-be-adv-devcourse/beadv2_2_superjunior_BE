package store._0982.elasticsearch.application.dto;

import store._0982.common.kafka.dto.ProductEvent;
import store._0982.elasticsearch.domain.GroupPurchaseDocument;
import java.time.OffsetDateTime;

public record GroupPurchaseDocumentInfo(
        String groupPurchaseId,
        String sellerName,
        Integer minQuantity,
        Integer maxQuantity,
        String title,
        String description,
        Long discountedPrice,
        String status,
        String startDate,
        String endDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Integer currentQuantity,
        ProductEvent productEvent
) {
    public static GroupPurchaseDocumentInfo from(GroupPurchaseDocument groupPurchaseDocument) {
        return new GroupPurchaseDocumentInfo(
                groupPurchaseDocument.getGroupPurchaseId(),
                groupPurchaseDocument.getSellerName(),
                groupPurchaseDocument.getMinQuantity(),
                groupPurchaseDocument.getMaxQuantity(),
                groupPurchaseDocument.getTitle(),
                groupPurchaseDocument.getDescription(),
                groupPurchaseDocument.getDiscountedPrice(),
                groupPurchaseDocument.getStatus(),
                groupPurchaseDocument.getStartDate(),
                groupPurchaseDocument.getEndDate(),
                groupPurchaseDocument.getCreatedAt(),
                groupPurchaseDocument.getUpdatedAt(),
                groupPurchaseDocument.getCurrentQuantity(),
                groupPurchaseDocument.getProductEvent()
        );
    }
}
