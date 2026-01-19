package store._0982.commerce.infrastructure.kafka.listener;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.commerce.application.product.ProductEmbeddingService;
import store._0982.commerce.application.product.dto.ProductEmbeddingCompleteInfo;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductEmbeddingCompletedEvent;
import store._0982.common.log.ServiceLog;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductVectorizeListener {

    private final ProductEmbeddingService embeddingService;
  
    @RetryableTopic
    @ServiceLog
    @KafkaListener(topics = KafkaTopics.PRODUCT_EMBEDDING_COMPLETED, groupId = "ai-service-group", containerFactory = "productEmbeddingCompleteEventKafkaListenerFactory")
    public void saveVector(ProductEmbeddingCompletedEvent completeEvent){
        ProductEmbeddingCompleteInfo info = embeddingService.updateEmbedding(completeEvent);
        log.info("{} 벡터 생성 완료.", info.productId());
    }
}
