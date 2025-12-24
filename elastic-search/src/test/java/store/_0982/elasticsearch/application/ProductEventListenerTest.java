package store._0982.elasticsearch.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(KafkaTestConfig.class)
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {
                KafkaTopics.PRODUCT_UPSERTED,
                KafkaTopics.PRODUCT_DELETED
        }
)
class ProductEventListenerTest {

    @Autowired
    private KafkaTemplate<String, ProductEvent> kafkaTemplate;

    @MockitoBean
    private ProductRepository productRepository;

    @Autowired
    private KafkaTestProbe probe;

    @BeforeEach
    void setUp() {
        probe.reset();
    }

    @Test
    @DisplayName("PRODUCT_UPSERTED 이벤트 수신 시 문서 저장")
    void product_upsert_event_consumed_and_saved() throws Exception {
        // given
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        ProductEvent event = new ProductEvent(
                productId,
                "테스트상품",
                1_200_000L,
                "KIDS",
                "테스트상품설명",
                10,
                "https://img.url",
                sellerId,
                "2025-01-01T00:00:00Z",
                "2025-01-01T00:00:00Z"
        );

        when(productRepository.save(any(ProductDocument.class)))
                .thenAnswer(invocation -> {
                    probe.markConsumed();
                    return invocation.getArgument(0);
                });

        // when
        kafkaTemplate.send(KafkaTopics.PRODUCT_UPSERTED, event).get();

        // then
        boolean consumed = probe.await(10, TimeUnit.SECONDS);
        assertThat(consumed).isTrue();

        ProductDocument saved = captureSavedProductDocument();
        assertThat(saved.getProductId()).isEqualTo(productId.toString());
        assertThat(saved.getSellerId()).isEqualTo(sellerId.toString());
        assertThat(saved.getName()).isEqualTo(event.getName());
        assertThat(saved.getPrice()).isEqualTo(event.getPrice());
        assertThat(saved.getCategory()).isEqualTo(event.getCategory());
        assertThat(saved.getDescription()).isEqualTo(event.getDescription());
        assertThat(saved.getStock()).isEqualTo(event.getStock());
        assertThat(saved.getOriginalUrl()).isEqualTo(event.getOriginalUrl());
        assertThat(saved.getCreatedAt()).isEqualTo(OffsetDateTime.parse(event.getCreatedAt()));
        assertThat(saved.getUpdatedAt()).isEqualTo(OffsetDateTime.parse(event.getUpdatedAt()));

        verify(productRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("PRODUCT_DELETED 이벤트 수신 시 문서 삭제")
    void product_delete_event_consumed_and_deleted() throws Exception {
        // given
        UUID productId = UUID.randomUUID();

        ProductEvent event = new ProductEvent(
                productId,
                null, null, null, null, null, null, null,
                null, null
        );

        doAnswer(invocation -> {
            probe.markConsumed();
            return null;
        }).when(productRepository).deleteById(anyString());

        // when
        kafkaTemplate.send(KafkaTopics.PRODUCT_DELETED, event).get();

        // then
        boolean consumed = probe.await(10, TimeUnit.SECONDS);
        assertThat(consumed).isTrue();

        verify(productRepository).deleteById(productId.toString());
        verify(productRepository, never()).save(any());
    }

    private ProductDocument captureSavedProductDocument() {
        ArgumentCaptor<ProductDocument> captor = ArgumentCaptor.forClass(ProductDocument.class);
        verify(productRepository).save(captor.capture());
        return captor.getValue();
    }
}
