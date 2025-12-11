package store._0982.elasticsearch.presentation.dto;

import store._0982.elasticsearch.application.dto.GroupPurchaseDocumentCommand;


import java.time.OffsetDateTime;

public record GroupPurchaseDocumentRequest(
        String groupPurchaseId,
        String productName,
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
        Integer currentQuantity
) {

    public GroupPurchaseDocumentCommand toCommand() {
        return new GroupPurchaseDocumentCommand(
                groupPurchaseId,
                productName,
                sellerName,
                minQuantity,
                maxQuantity,
                title,
                description,
                discountedPrice,
                status,
                startDate,
                endDate,
                createdAt,
                updatedAt,
                currentQuantity
        );
    }
}
