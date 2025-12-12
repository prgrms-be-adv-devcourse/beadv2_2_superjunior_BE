package store._0982.elasticsearch.application.dto;

import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.common.kafka.dto.ProductEvent;
import store._0982.elasticsearch.domain.GroupPurchaseDocument;
import java.time.OffsetDateTime;

public record GroupPurchaseDocumentCommand(
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
    public static GroupPurchaseDocumentCommand from(GroupPurchaseEvent event) {
        return new GroupPurchaseDocumentCommand(
                event.getId().toString(),
                event.getSellerName(),
                event.getMinQuantity(),
                event.getMaxQuantity(),
                event.getTitle(),
                event.getDescription(),
                event.getDiscountedPrice(),
                event.getStatus(),
                event.getStartDate(),
                event.getEndDate(),
                OffsetDateTime.parse(event.getCreatedAt()),
                OffsetDateTime.parse(event.getUpdatedAt()),
                event.getCurrentQuantity(),
                event.getProductEvent()
        );
    }

    public GroupPurchaseDocument toDocument() {
        return GroupPurchaseDocument.builder()
                .groupPurchaseId(groupPurchaseId)
                .sellerName(sellerName)
                .minQuantity(minQuantity)
                .maxQuantity(maxQuantity)
                .title(title)
                .description(description)
                .discountedPrice(discountedPrice)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .currentQuantity(currentQuantity)
                .productEvent(productEvent)
                .build();
    }
}
