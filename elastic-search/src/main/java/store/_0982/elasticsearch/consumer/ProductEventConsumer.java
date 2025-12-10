package store._0982.elasticsearch.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import store._0982.common.dto.event.ProductEvent;
import store._0982.elasticsearch.application.ProductSearchService;
import store._0982.elasticsearch.application.dto.ProductDocumentCommand;

@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final ProductSearchService productSearchService;

    @KafkaListener(topics = "product.upsert", groupId = "search-index-group")
    public void create(ProductEvent event) {
        ProductDocumentCommand command = ProductDocumentCommand.from(event);
        productSearchService.saveProductDocument(command);
    }
}
