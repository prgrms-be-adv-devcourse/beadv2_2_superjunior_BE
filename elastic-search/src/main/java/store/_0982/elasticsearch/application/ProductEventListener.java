package store._0982.elasticsearch.application;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductEvent;
import store._0982.common.log.ServiceLog;
import store._0982.elasticsearch.application.dto.ProductDocumentCommand;
import store._0982.elasticsearch.application.dto.ProductDocumentInfo;
import store._0982.elasticsearch.infrastructure.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductRepository productRepository;

    @RetryableTopic
    @ServiceLog
    @KafkaListener(topics = KafkaTopics.PRODUCT_UPSERTED, groupId = "search-service-group", containerFactory = "upsertProductKafkaListenerFactory")
    public void upsert(ProductEvent event) {
        ProductDocumentCommand command = ProductDocumentCommand.from(event);
        productRepository.save(command.toDocument());
    }

    @RetryableTopic
    @ServiceLog
    @KafkaListener(topics = KafkaTopics.PRODUCT_DELETED, groupId = "search-service-group", containerFactory = "deleteProductKafkaListenerFactory")
    public void delete(ProductEvent event) {
        productRepository.deleteById(event.getId().toString());
    }

    public ProductDocumentInfo saveProductDocument(ProductDocumentCommand command) {
        return ProductDocumentInfo.from(productRepository.save(command.toDocument()));
    }

}
