package store._0982.elasticsearch.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.stereotype.Service;
import store._0982.common.dto.PageResponse;
import store._0982.common.log.ServiceLog;
import store._0982.elasticsearch.application.dto.GroupPurchaseSearchInfo;
import store._0982.elasticsearch.domain.GroupPurchaseDocument;
import store._0982.elasticsearch.domain.search.GroupPurchaseSearchRepository;
import store._0982.elasticsearch.domain.search.GroupPurchaseSearchRow;
import store._0982.elasticsearch.exception.ElasticsearchExceptionTranslator;
import store._0982.elasticsearch.exception.ElasticsearchExecutor;
import store._0982.elasticsearch.infrastructure.queryfactory.GroupPurchaseSimilarityQueryFactory;
import store._0982.elasticsearch.infrastructure.queryfactory.GroupPurchaseSearchQueryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class GroupPurchaseSearchService {

    private final ElasticsearchOperations operations;
    private final GroupPurchaseSearchQueryFactory groupPurchaseSearchQueryFactory;
    private final GroupPurchaseSimilarityQueryFactory groupPurchaseSimilarityQueryFactory;
    private final ElasticsearchExceptionTranslator exceptionTranslator;
    private final ElasticsearchExecutor elasticsearchExecutor;
    private final GroupPurchaseSearchRepository groupPurchaseSearchRepository;

    private static final long[] SEARCH_RETRY_DELAYS_MS = {200L, 500L};

    @ServiceLog
    public PageResponse<GroupPurchaseSearchInfo> searchGroupPurchaseDocument(
            String keyword,
            String status,
            UUID memberId,
            String category,
            Pageable pageable
    ) {
        String sellerId = memberId != null ? memberId.toString() : null;
        return elasticsearchExecutor.execute(() -> {
            NativeQuery query = groupPurchaseSearchQueryFactory.createSearchQuery(keyword, status, sellerId, category, pageable);

            SearchHits<GroupPurchaseDocument> hits = searchWithRetry(query);
            Page<GroupPurchaseSearchInfo> mappedPage = toSearchResultPage(hits, pageable, null);
            return PageResponse.from(mappedPage);
        });
    }

    @ServiceLog
    public PageResponse<GroupPurchaseSearchInfo> searchGroupPurchaseByVector(
            float[] vector,
            Pageable pageable
    ) {
        return elasticsearchExecutor.execute(() -> {
            NativeQuery query = groupPurchaseSimilarityQueryFactory.createSimilarityQuery(vector, pageable);
            SearchHits<GroupPurchaseDocument> hits = searchWithRetry(query);
            Page<GroupPurchaseSearchInfo> mappedPage = toSearchResultPage(hits, pageable, toScoreMap(hits));
            return PageResponse.from(mappedPage);
        });
    }

    private SearchHits<GroupPurchaseDocument> searchWithRetry(NativeQuery query) {
        int attempts = SEARCH_RETRY_DELAYS_MS.length + 1;
        for (int i = 0; i < attempts; i++) {
            try {
                return operations.search(query, GroupPurchaseDocument.class);
            } catch (Exception e) {
                if (!exceptionTranslator.isRetryable(e) || i == attempts - 1) {
                    throw exceptionTranslator.translate(e);
                }
                try {
                    Thread.sleep(SEARCH_RETRY_DELAYS_MS[i]);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw exceptionTranslator.translate(interruptedException);
                }
            }
        }
        throw exceptionTranslator.translate(new IllegalStateException("search retry exhausted"));
    }

    private Page<GroupPurchaseSearchInfo> toSearchResultPage(
            SearchHits<GroupPurchaseDocument> hits,
            Pageable pageable,
            Map<UUID, Double> scores
    ) {
        if (hits.getSearchHits().isEmpty()) {
            return new PageImpl<>(List.of(), pageable, hits.getTotalHits());
        }

        List<UUID> ids = hits.getSearchHits()
                .stream()
                .map(hit -> UUID.fromString(hit.getId()))
                .toList();

        List<GroupPurchaseSearchRow> rows = groupPurchaseSearchRepository.findAllByIds(ids);
        Map<UUID, GroupPurchaseSearchRow> rowMap = rows.stream()
                .collect(Collectors.toMap(GroupPurchaseSearchRow::groupPurchaseId, Function.identity()));

        List<GroupPurchaseSearchInfo> ordered = new ArrayList<>(ids.size());
        for (UUID id : ids) {
            GroupPurchaseSearchRow row = rowMap.get(id);
            if (row != null) {
                Double score = scores != null ? scores.get(id) : null;
                ordered.add(GroupPurchaseSearchInfo.from(row, score));
            }
        }

        return new PageImpl<>(ordered, pageable, hits.getTotalHits());
    }

    private Map<UUID, Double> toScoreMap(SearchHits<GroupPurchaseDocument> hits) {
        return hits.getSearchHits().stream()
                .collect(Collectors.toMap(
                        hit -> UUID.fromString(hit.getId()),
                        hit -> (double) hit.getScore(),
                        (left, right) -> left
                ));
    }
}
