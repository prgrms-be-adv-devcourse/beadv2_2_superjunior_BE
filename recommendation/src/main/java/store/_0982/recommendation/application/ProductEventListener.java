package store._0982.recommendation.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductUpsertedEvent;
import store._0982.common.log.ServiceLog;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventListener {

    private final EmbeddingService embeddingService;
    private final ChatbotIndexService chatbotIndexService;

    @RetryableTopic
    @ServiceLog
    @KafkaListener(
            topics = KafkaTopics.PRODUCT_UPSERTED,
            groupId = "ai-service-group",
            containerFactory = "productEmbeddingEventKafkaListenerFactory"
    )
    public void vectorize(ProductUpsertedEvent event) {
        // 기존: product_vector 테이블에 임베딩 저장
        embeddingService.vectorize(event);

        // 추가: chatbot_embed 테이블에 VectorStore Document 저장
        chatbotIndexService.indexFromEvent(event);
    }
}
