package store._0982.elasticsearch.application.dto;

import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.common.kafka.dto.ProductEvent;
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
        ProductEvent productEvent
) {
    public static GroupPurchaseDocumentCommand from(GroupPurchaseEvent event) {
        return new GroupPurchaseDocumentCommand(
                event.getId().toString(),
                event.getTitle(),
                event.getDescription(),
                event.getStatus(),
                event.getDiscountedPrice(),
                event.getCurrentQuantity(),
                OffsetDateTime.parse(event.getEndDate()),
                OffsetDateTime.parse(event.getUpdatedAt()),
                event.getProductEvent()
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
                .discountRate(calculateDiscountRate(productEvent, discountedPrice))
                .productDocumentEmbedded(ProductDocumentEmbedded.from(productEvent))
                .build();
    }

    private static Long calculateDiscountRate(ProductEvent productEvent, Long discountedPrice) {
        if (productEvent == null) {
            return 0L;
        }
        return calculateDiscountRate(productEvent.getPrice(), discountedPrice);
    }

    private static Long calculateDiscountRate(Long price, Long discountedPrice) {
        if (price == null || discountedPrice == null) {
            return 0L;
        }

        if (price <= 0 || discountedPrice >= price) {
            return 0L;
        }

        return Math.round(
                ((double) (price - discountedPrice) / price) * 100
        );
    }
}
