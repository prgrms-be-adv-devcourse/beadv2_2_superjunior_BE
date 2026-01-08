package store._0982.elasticsearch.presentation.dto;

import store._0982.common.kafka.dto.ProductEvent;
import store._0982.elasticsearch.application.dto.GroupPurchaseDocumentCommand;


import java.time.OffsetDateTime;

public record GroupPurchaseDocumentRequest(
        String groupPurchaseId,
        String title,
        String description,
        String status,
        OffsetDateTime updatedAt,
        ProductEvent productEvent
) {

    public GroupPurchaseDocumentCommand toCommand() {
        return new GroupPurchaseDocumentCommand(
                groupPurchaseId,
                title,
                description,
                status,
                updatedAt,
                productEvent
        );
    }
}
