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
    private ProductSearchQueryFactory queryFactory;

    @Mock
    private IndexOperations indexOperations;

    @InjectMocks
    private ProductSearchService service;

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
        service.createProductIndex();

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
        service.createProductIndex();

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
        service.deleteProductIndex();

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
        service.deleteProductIndex();

        // then
        verify(indexOperations, never()).delete();
    }

    //test es 접속해서 하도록 수정 필요
    @Test
    @DisplayName("상품 문서 검색 성공")
    void searchProductDocument_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "아이폰";
        UUID sellerId = UUID.randomUUID();
        String category = "KIDS";

        NativeQuery query = mock(NativeQuery.class);

        when(queryFactory.build(keyword, sellerId, category, pageable))
                .thenReturn(query);

        ProductDocument document = ProductDocument.builder()
                .productId("p-1")
                .name("아이폰 15")
                .category(category)
                .sellerId(sellerId.toString())
                .build();

        SearchHit<ProductDocument> hit = mock(SearchHit.class);
        when(hit.getContent()).thenReturn(document);

        SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getSearchHits()).thenReturn(List.of(hit));
        when(searchHits.getTotalHits()).thenReturn(1L);

        when(operations.search(query, ProductDocument.class))
                .thenReturn(searchHits);

        // when
        PageResponse<ProductDocumentInfo> response =
                service.searchProductDocument(keyword, sellerId, category, pageable);

        // then
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).name()).isEqualTo("아이폰 15");

        verify(queryFactory).build(keyword, sellerId, category, pageable);
        verify(operations).search(query, ProductDocument.class);
    }
}
