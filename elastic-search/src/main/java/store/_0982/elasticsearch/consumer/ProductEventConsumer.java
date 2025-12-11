package store._0982.elasticsearch.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductEvent;
import store._0982.elasticsearch.application.ProductSearchService;
import store._0982.elasticsearch.application.dto.ProductDocumentCommand;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final ProductSearchService productSearchService;

    @RetryableTopic
    @KafkaListener(topics = KafkaTopics.PRODUCT_UPSERTED, groupId = "search-service-group", containerFactory = "upsertProductKafkaListenerFactory")
    public void upsert(ProductEvent event) {
        ProductDocumentCommand command = ProductDocumentCommand.from(event);
        productSearchService.saveProductDocument(command);
    }

    @RetryableTopic
    @KafkaListener(topics = KafkaTopics.PRODUCT_DELETED, groupId = "search-service-group", containerFactory = "deleteProductKafkaListenerFactory")
    public void delete(UUID id) {
        productSearchService.deleteProductDocument(id);
    }
}
