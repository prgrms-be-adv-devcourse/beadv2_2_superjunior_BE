package store._0982.elasticsearch.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductEvent;
import store._0982.elasticsearch.application.support.KafkaTestProbe;
import store._0982.elasticsearch.config.KafkaTestConfig;
import store._0982.elasticsearch.domain.ProductDocument;
import store._0982.elasticsearch.infrastructure.ProductRepository;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(KafkaTestConfig.class)
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = KafkaTopics.PRODUCT_UPSERTED
)
class ProductEventListenerTest {

    @Autowired
    private KafkaTemplate<String, ProductEvent> kafkaTemplate;

    @MockitoBean
    private ProductRepository productRepository;

    @Autowired
    private KafkaTestProbe probe;

    @Test
    @DisplayName("PRODUCT_UPSERTED 이벤트 수신 시 상품 문서 저장")
    void product_upsert_event_consumed_and_saved() throws Exception {
        // given
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        ProductEvent event = new ProductEvent(
                productId,
                "아이폰 15",
                1_200_000L,
                "KIDS",
                "아이폰 설명",
                10,
                "https://img.url",
                sellerId,
                "2025-01-01T00:00:00Z",
                "2025-01-01T00:00:00Z"
        );

        when(productRepository.save(any(ProductDocument.class)))
                .thenAnswer(invocation -> {
                    probe.markConsumed(); // 🔥 Kafka 메시지 소비 확인
                    return invocation.getArgument(0);
                });

        // when (Kafka로 실제 메시지 발행)
        kafkaTemplate.send(KafkaTopics.PRODUCT_UPSERTED, event).get();

        // then
        boolean consumed = probe.await(10, TimeUnit.SECONDS);
        assertThat(consumed).isTrue();

        verify(productRepository).save(any(ProductDocument.class));
        verify(productRepository, never()).deleteById(any());
    }
}
