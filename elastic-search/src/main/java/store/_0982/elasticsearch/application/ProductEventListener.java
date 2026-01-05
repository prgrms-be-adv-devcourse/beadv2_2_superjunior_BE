package store._0982.elasticsearch.application;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductEvent;
import store._0982.common.log.ServiceLog;
import store._0982.elasticsearch.application.dto.ProductDocumentCommand;
import store._0982.elasticsearch.exception.ElasticsearchExceptionTranslator;
import store._0982.elasticsearch.infrastructure.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductRepository productRepository;
    private final ElasticsearchExceptionTranslator exceptionTranslator;

    @RetryableTopic
    @ServiceLog
    @KafkaListener(topics = KafkaTopics.PRODUCT_UPSERTED, groupId = "search-service-group", containerFactory = "upsertProductKafkaListenerFactory")
    public void upsert(ProductEvent event) {
        try {
            ProductDocumentCommand command = ProductDocumentCommand.from(event);
            productRepository.save(command.toDocument());
        } catch (Exception e) {
            throw exceptionTranslator.translate(e);
        }
    }

    @RetryableTopic
    @ServiceLog
    @KafkaListener(topics = KafkaTopics.PRODUCT_DELETED, groupId = "search-service-group", containerFactory = "deleteProductKafkaListenerFactory")
    public void delete(ProductEvent event) {
        try {
            productRepository.deleteById(event.getId().toString());
        } catch (Exception e) {
            throw exceptionTranslator.translate(e);
        }
    }

}
