package store._0982.commerce.application.product;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.log.ServiceLog;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductVectorizeListener {

    @RetryableTopic
    @ServiceLog
    @KafkaListener(topics = KafkaTopics.PRODUCT_EMBEDDING_COMPLETED, groupId = "ai-service-group", containerFactory = "productEmbeddingCompleteEventKafkaListenerFactory")
    public void saveVector(ProductEmbeddingCompleteEvent completeEvent){
        log.info("embedding size={}, productId={}", completeEvent.getVector().length, completeEvent.getProductId());
    }
}
