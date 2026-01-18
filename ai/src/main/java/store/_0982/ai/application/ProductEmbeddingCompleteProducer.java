package store._0982.ai.application;


import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductEmbeddingCompletedEvent;


@Service
@RequiredArgsConstructor
public class ProductEmbeddingCompleteProducer {

    private final KafkaTemplate<String, ProductEmbeddingCompletedEvent> kafkaTemplate;

    public void returnVector(ProductEmbeddingCompletedEvent completeEvent) {
        kafkaTemplate.send(KafkaTopics.PRODUCT_EMBEDDING_COMPLETED, completeEvent.getProductId().toString(), completeEvent);
    }
}
