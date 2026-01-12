package store._0982.elasticsearch.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.document.Document;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;
import store._0982.elasticsearch.exception.ElasticsearchExceptionTranslator;
import store._0982.elasticsearch.exception.CustomErrorCode;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductSearchServiceTest {

    @Mock
    private ElasticsearchOperations operations;

    @Mock
    private ProductSearchQueryFactory productSearchQueryFactory;

    @Mock
    private IndexOperations indexOperations;

    @Spy
    private ElasticsearchExceptionTranslator exceptionTranslator = new ElasticsearchExceptionTranslator();

    @InjectMocks
    private ProductSearchService productSearchService;

    @Test
    @DisplayName("상품 인덱스 생성 성공")
    void createProductIndex_success() {
        // given
        when(operations.indexOps(ProductDocument.class))
                .thenReturn(indexOperations);

        when(indexOperations.exists())
                .thenReturn(false);

        when(indexOperations.create(any(Document.class)))
                .thenReturn(true);

        when(indexOperations.createMapping(ProductDocument.class))
                .thenReturn(Document.create());

        // when
        productSearchService.createProductIndex();

        // then
        verify(indexOperations).create(any(Document.class));
        verify(indexOperations).putMapping(any(Document.class));
    }

    @Test
    @DisplayName("이미 존재하는 상품 인덱스가 있으면 생성하지 않는다")
    void createProductIndex_alreadyExists_skip() {
        // given
        when(operations.indexOps(ProductDocument.class))
                .thenReturn(indexOperations);
        when(indexOperations.exists())
                .thenReturn(true);

        // when
        productSearchService.createProductIndex();

        // then
        verify(indexOperations, never()).create(any());
        verify(indexOperations, never()).putMapping(any(Document.class));
    }

    @Test
    @DisplayName("상품 인덱스 삭제 성공")
    void deleteProductIndex_success() {
        // given
        when(operations.indexOps(ProductDocument.class))
                .thenReturn(indexOperations);

        when(indexOperations.exists())
                .thenReturn(true);

        when(indexOperations.delete())
                .thenReturn(true);

        // when
        productSearchService.deleteProductIndex();

        // then
        verify(indexOperations).delete();
    }

    @Test
    @DisplayName("존재하지 않는 상품 인덱스가 있으면 삭제하지 않는다")
    void deleteProductIndex_notExists_skip() {
        // given
        when(operations.indexOps(ProductDocument.class))
                .thenReturn(indexOperations);
        when(indexOperations.exists())
                .thenReturn(false);

        // when
        productSearchService.deleteProductIndex();

        // then
        verify(indexOperations, never()).delete();
    }

    @Test
    @DisplayName("상품 검색 시 검색 결과를 PageResponse로 반환한다")
    void search_product_document_success() {
        // given
        String keyword = "아이폰";
        UUID sellerId = UUID.randomUUID();
        String category = "KIDS";
        Pageable pageable = PageRequest.of(0, 10);

        NativeQuery query = mock(NativeQuery.class);

        ProductDocument document = ProductDocument.builder()
                .productId("product-id")
                .name("아이폰 15")
                .price(1_200_000L)
                .category("KIDS")
                .sellerId(sellerId.toString())
                .build();

        SearchHit<ProductDocument> hit = new SearchHit<>(
                "product-index",
                "product-id",
                null,
                1.0f,
                null,
                null,
                null,
                null,
                null,
                null,
                document
        );

        SearchHits<ProductDocument> searchHits =
                new SearchHitsImpl<>(
                        1L,
                        TotalHitsRelation.EQUAL_TO,
                        1.0f,
                        null,
                        null,
                        null,
                        List.of(hit),
                        null,
                        null,
                        null
                );

        when(productSearchQueryFactory.build(keyword, sellerId, category, pageable))
                .thenReturn(query);

        when(operations.search(query, ProductDocument.class))
                .thenReturn(searchHits);

        // when
        PageResponse<ProductDocumentInfo> response =
                productSearchService.searchProductDocument(
                        keyword, sellerId, category, pageable
                );

        // then
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).name()).isEqualTo("아이폰 15");

        verify(productSearchQueryFactory).build(keyword, sellerId, category, pageable);
        verify(operations).search(query, ProductDocument.class);
    }

    @Test
    @DisplayName("SERVICE_UNAVAILABLE 예외 처리")
    void search_product_document_service_unavailable() {
        // given
        String keyword = "keyword";
        UUID sellerId = UUID.randomUUID();
        String category = "KIDS";
        Pageable pageable = PageRequest.of(0, 10);

        NativeQuery query = mock(NativeQuery.class);
        when(productSearchQueryFactory.build(keyword, sellerId, category, pageable))
                .thenReturn(query);

        when(operations.search(query, ProductDocument.class))
                .thenThrow(new DataAccessResourceFailureException("ES down"));

        // when & then
        assertThatThrownBy(() -> productSearchService.searchProductDocument(keyword, sellerId, category, pageable))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(CustomErrorCode.SERVICE_UNAVAILABLE);
    }

    @Test
    @DisplayName("INTERNAL_SERVER_ERROR 예외 처리")
    void search_product_document_internal_server_error() {
        // given
        String keyword = "keyword";
        UUID sellerId = UUID.randomUUID();
        String category = "KIDS";
        Pageable pageable = PageRequest.of(0, 10);

        NativeQuery query = mock(NativeQuery.class);
        when(productSearchQueryFactory.build(keyword, sellerId, category, pageable))
                .thenReturn(query);

        when(operations.search(query, ProductDocument.class))
                .thenThrow(new RuntimeException("boom"));

        // when & then
        assertThatThrownBy(() -> productSearchService.searchProductDocument(keyword, sellerId, category, pageable))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(CustomErrorCode.INTERNAL_SERVER_ERROR);
    }
    @Test
    @DisplayName("SERVICE_UNAVAILABLE 오류 발생 시 재시도")
    void search_product_document_retries_on_service_unavailable() {
        // given
        String keyword = "keyword";
        UUID sellerId = UUID.randomUUID();
        String category = "KIDS";
        Pageable pageable = PageRequest.of(0, 10);

        NativeQuery query = mock(NativeQuery.class);
        when(productSearchQueryFactory.build(keyword, sellerId, category, pageable))
                .thenReturn(query);

        ProductDocument document = ProductDocument.builder()
                .productId("product-id")
                .name("product-1")
                .price(1_200_000L)
                .category("KIDS")
                .sellerId(sellerId.toString())
                .build();

        SearchHit<ProductDocument> hit = new SearchHit<>(
                "product-index",
                "product-id",
                null,
                1.0f,
                null,
                null,
                null,
                null,
                null,
                null,
                document
        );

        SearchHits<ProductDocument> searchHits =
                new SearchHitsImpl<>(
                        1L,
                        TotalHitsRelation.EQUAL_TO,
                        1.0f,
                        null,
                        null,
                        null,
                        List.of(hit),
                        null,
                        null,
                        null
                );

        when(operations.search(query, ProductDocument.class))
                .thenThrow(new DataAccessResourceFailureException("ES down"))
                .thenReturn(searchHits);

        // when
        PageResponse<ProductDocumentInfo> response =
                productSearchService.searchProductDocument(keyword, sellerId, category, pageable);

        // then
        assertThat(response.content()).hasSize(1);
        verify(operations, times(2)).search(query, ProductDocument.class);
    }
    @Test
    @DisplayName("재시도 횟수 소진 시 SERVICE_UNAVAILABLE")
    void search_product_document_retry_exhausted_returns_service_unavailable() {
        // given
        String keyword = "keyword";
        UUID sellerId = UUID.randomUUID();
        String category = "KIDS";
        Pageable pageable = PageRequest.of(0, 10);

        NativeQuery query = mock(NativeQuery.class);
        when(productSearchQueryFactory.build(keyword, sellerId, category, pageable))
                .thenReturn(query);

        when(operations.search(query, ProductDocument.class))
                .thenThrow(new DataAccessResourceFailureException("ES down"))
                .thenThrow(new DataAccessResourceFailureException("ES down"))
                .thenThrow(new DataAccessResourceFailureException("ES down"));

        // when & then
        assertThatThrownBy(() -> productSearchService.searchProductDocument(keyword, sellerId, category, pageable))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(CustomErrorCode.SERVICE_UNAVAILABLE);

        verify(operations, times(3)).search(query, ProductDocument.class);
    }
}
