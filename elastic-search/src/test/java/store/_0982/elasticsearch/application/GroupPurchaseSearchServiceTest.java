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
import store._0982.elasticsearch.application.dto.GroupPurchaseDocumentInfo;
import store._0982.elasticsearch.domain.GroupPurchaseDocument;
import store._0982.elasticsearch.domain.ProductDocumentEmbedded;
import store._0982.elasticsearch.infrastructure.queryfactory.GroupPurchaseSearchQueryFactory;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupPurchaseSearchServiceTest {

    @Mock
    private ElasticsearchOperations operations;

    @Mock
    private GroupPurchaseSearchQueryFactory queryFactory;

    @Mock
    private IndexOperations indexOperations;

    @InjectMocks
    private GroupPurchaseSearchService service;

    @Test
    @DisplayName("공동구매 인덱스 생성 성공")
    void createGroupPurchaseIndex_success() {
        // given
        when(operations.indexOps(GroupPurchaseDocument.class))
                .thenReturn(indexOperations);
        when(indexOperations.exists())
                .thenReturn(false);
        when(indexOperations.create(any(Document.class)))
                .thenReturn(true);
        when(indexOperations.createMapping(GroupPurchaseDocument.class))
                .thenReturn(Document.create());

        // when
        service.createGroupPurchaseIndex();

        // then
        verify(indexOperations).create(any(Document.class));
        verify(indexOperations).putMapping(any(Document.class));
    }

    @Test
    @DisplayName("이미 공동구매 인덱스가 존재하면 생성하지 않는다")
    void createGroupPurchaseIndex_alreadyExists_skip() {
        // given
        when(operations.indexOps(GroupPurchaseDocument.class))
                .thenReturn(indexOperations);
        when(indexOperations.exists())
                .thenReturn(true);

        // when
        service.createGroupPurchaseIndex();

        // then
        verify(indexOperations, never()).create(any());
        verify(indexOperations, never()).putMapping(any(Document.class));
    }

    @Test
    @DisplayName("공동구매 인덱스 삭제 성공")
    void deleteGroupPurchaseIndex_success() {
        // given
        when(operations.indexOps(GroupPurchaseDocument.class))
                .thenReturn(indexOperations);
        when(indexOperations.exists())
                .thenReturn(true);
        when(indexOperations.delete())
                .thenReturn(true);

        // when
        service.deleteGroupPurchaseIndex();

        // then
        verify(indexOperations).delete();
    }

    @Test
    @DisplayName("존재하지 않는 공동구매 인덱스는 삭제하지 않는다")
    void deleteGroupPurchaseIndex_notExists_skip() {
        // given
        when(operations.indexOps(GroupPurchaseDocument.class))
                .thenReturn(indexOperations);
        when(indexOperations.exists())
                .thenReturn(false);

        // when
        service.deleteGroupPurchaseIndex();

        // then
        verify(indexOperations, never()).delete();
    }

    @Test
    @DisplayName("회원 기준 공동구매 검색 성공")
    void searchGroupPurchaseDocument_success() {
        // given
        String keyword = "아이폰";
        String status = "OPEN";
        UUID memberId = UUID.randomUUID();
        String category = "DIGITAL";
        Pageable pageable = PageRequest.of(0, 10);

        NativeQuery query = mock(NativeQuery.class);

        GroupPurchaseDocument document = GroupPurchaseDocument.builder()
                .groupPurchaseId("gp-1")
                .title("아이폰 공동구매")
                .status("OPEN")
                .productDocumentEmbedded(
                        new ProductDocumentEmbedded(
                                "product-1",
                                "DIGITAL",     // ✅ category는 여기
                                1_200_000L,
                                "https://img.url",
                                memberId.toString()
                        )
                )
                .build();

        SearchHit<GroupPurchaseDocument> hit = new SearchHit<>(
                "group-purchase-index",
                "gp-1",
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

        SearchHits<GroupPurchaseDocument> searchHits =
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

        when(queryFactory.createSearchQuery(keyword, status, memberId.toString(), category, pageable))
                .thenReturn(query);

        when(operations.search(query, GroupPurchaseDocument.class))
                .thenReturn(searchHits);

        // when
        PageResponse<GroupPurchaseDocumentInfo> response =
                service.searchGroupPurchaseDocument(
                        keyword, status, memberId, category, pageable
                );

        // then
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).title()).isEqualTo("아이폰 공동구매");
        assertThat(response.content().get(0).productDocumentEmbedded().getSellerId()).isEqualTo(memberId.toString());
        assertThat(response.content().get(0).productDocumentEmbedded().getCategory()).isEqualTo("DIGITAL");

        verify(queryFactory)
                .createSearchQuery(keyword, status, memberId.toString(), category, pageable);
        verify(operations)
                .search(query, GroupPurchaseDocument.class);
    }

    @Test
    @DisplayName("전체 공동구매 검색 성공 (memberId 없음)")
    void searchAllGroupPurchaseDocument_success() {
        // given
        String keyword = "아이폰";
        String status = "OPEN";
        String category = "DIGITAL";
        Pageable pageable = PageRequest.of(0, 10);

        NativeQuery query = mock(NativeQuery.class);

        GroupPurchaseDocument document = GroupPurchaseDocument.builder()
                .groupPurchaseId("gp-1")
                .title("아이폰 전체 공동구매")
                .status("OPEN")
                .productDocumentEmbedded(
                        new ProductDocumentEmbedded(
                                "product-1",
                                "DIGITAL",     // ✅ category는 여기
                                1_200_000L,
                                "https://img.url",
                                "seller-id"
                        )
                )
                .build();

        SearchHit<GroupPurchaseDocument> hit = new SearchHit<>(
                "group-purchase-index",
                "gp-2",
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

        SearchHits<GroupPurchaseDocument> searchHits =
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

        when(queryFactory.createSearchQuery(keyword, status, null, category, pageable))
                .thenReturn(query);

        when(operations.search(query, GroupPurchaseDocument.class))
                .thenReturn(searchHits);

        // when
        PageResponse<GroupPurchaseDocumentInfo> response =
                service.searchAllGroupPurchaseDocument(
                        keyword, status, category, pageable
                );

        // then
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).title()).isEqualTo("아이폰 전체 공동구매");
        assertThat(response.content().get(0).productDocumentEmbedded().getCategory()).isEqualTo("DIGITAL");

        verify(queryFactory)
                .createSearchQuery(keyword, status, null, category, pageable);
        verify(operations)
                .search(query, GroupPurchaseDocument.class);
    }
}
