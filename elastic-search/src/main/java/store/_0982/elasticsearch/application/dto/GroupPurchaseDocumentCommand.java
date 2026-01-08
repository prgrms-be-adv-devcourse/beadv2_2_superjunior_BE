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
        OffsetDateTime updatedAt,
        ProductEvent productEvent
) {
    public static GroupPurchaseDocumentCommand from(GroupPurchaseEvent event) {
        return new GroupPurchaseDocumentCommand(
                event.getId().toString(),
                event.getTitle(),
                event.getDescription(),
                event.getStatus(),
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
                .updatedAt(updatedAt)
                .productDocumentEmbedded(ProductDocumentEmbedded.from(productEvent))
                .build();
    }
}
