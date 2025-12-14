package store._0982.elasticsearch.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.common.kafka.dto.ProductEvent;
import store._0982.elasticsearch.domain.ProductDocument;
import store._0982.elasticsearch.infrastructure.ProductRepository;

import java.time.Clock;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductEventListenerTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductEventListener listener;

    @Test
    @DisplayName("PRODUCT_UPSERTED 이벤트 수신 시 상품 문서 저장")
    void upsert_event_success() {
        // given
        UUID productId = UUID.randomUUID();

        ProductEvent event = new ProductEvent(
                Clock.systemUTC(),
                productId,
                "아이폰 15",          // name
                1_200_000L,          // price
                "KIDS",              // category
                "아이폰 설명",        // description
                10,                  // stock
                "https://img.url",   // originalUrl
                UUID.randomUUID(),   // sellerId
                "2025-01-01T00:00:00Z",
                "2025-01-01T00:00:00Z"
        );

        when(productRepository.save(any()))
                .thenReturn(mock(ProductDocument.class));

        // when
        listener.upsert(event);

        // then
        verify(productRepository).save(any(ProductDocument.class));
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("PRODUCT_DELETED 이벤트 수신 시 상품 문서 삭제")
    void delete_event_success() {
        // given
        UUID productId = UUID.randomUUID();

        ProductEvent event = new ProductEvent(
                Clock.systemUTC(),
                productId,
                null,
                100L,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // when
        listener.delete(event);

        // then
        verify(productRepository).deleteById(productId.toString());
        verify(productRepository, never()).save(any());
    }
}
