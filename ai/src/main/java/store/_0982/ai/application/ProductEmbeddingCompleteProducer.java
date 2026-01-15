package store._0982.ai.application;


import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductEmbeddingCompleteEvent;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductEmbeddingCompleteProducer {

    private final KafkaTemplate<UUID, ProductEmbeddingCompleteEvent> kafkaTemplate;

    public void returnVector(ProductEmbeddingCompleteEvent completeEvent){
        kafkaTemplate.send(KafkaTopics.PRODUCT_EMBEDDING_COMPLETED, completeEvent.getProductId(), completeEvent);
    }
}
