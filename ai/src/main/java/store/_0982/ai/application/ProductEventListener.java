package store._0982.ai.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductEmbeddingCompleteEvent;
import store._0982.common.kafka.dto.ProductEmbeddingEvent;
import store._0982.common.log.ServiceLog;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventListener {

    private final AiService aiService;
    private final ProductEmbeddingCompleteProducer completeProducer;

    @RetryableTopic
    @ServiceLog
    @KafkaListener(topics = KafkaTopics.PRODUCT_EMBEDDING_REQUESTED, groupId = "ai-service-group", containerFactory = "productEmbeddingEventKafkaListenerFactory")
    public void vectorize(ProductEmbeddingEvent event) {
        // 벡터화
        ProductEmbeddingCompleteEvent completeEvent = aiService.vectorize(event);
        // 재전송
        completeProducer.returnVector(completeEvent);
        log.info("재전송 완료");
    }
}
