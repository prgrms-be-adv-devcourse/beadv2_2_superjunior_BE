package store._0982.ai.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductEmbeddingCompleteEvent;
import store._0982.common.kafka.dto.ProductEmbeddingCompletedEvent;
import store._0982.common.kafka.dto.ProductUpsertedEvent;
import store._0982.common.log.ServiceLog;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventListener {

    private final AiService aiService;
    private final ProductEmbeddingCompleteProducer completeProducer;

    @RetryableTopic
    @ServiceLog
    @KafkaListener(topics = KafkaTopics.PRODUCT_UPSERTED, groupId = "ai-service-group", containerFactory = "productEmbeddingEventKafkaListenerFactory")
    public void vectorize(ProductUpsertedEvent event) {
        // 벡터화
        ProductEmbeddingCompletedEvent completeEvent = aiService.vectorize(event);
        // 재전송
        completeProducer.returnVector(completeEvent);
        log.info("재전송 완료");
    }
}
