package store._0982.elasticsearch.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.document.Document;
import store._0982.common.dto.PageResponse;
import store._0982.elasticsearch.application.dto.ProductDocumentInfo;
import store._0982.elasticsearch.domain.ProductDocument;
import store._0982.elasticsearch.infrastructure.queryfactory.ProductSearchQueryFactory;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
}
