package store._0982.elasticsearch.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Service;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.elasticsearch.application.dto.GroupPurchaseDocumentIdInfo;
import store._0982.elasticsearch.domain.GroupPurchaseDocument;
import store._0982.elasticsearch.exception.ElasticsearchExceptionTranslator;
import store._0982.elasticsearch.infrastructure.queryfactory.GroupPurchaseSearchQueryFactory;

import java.util.UUID;


@RequiredArgsConstructor
@Service
public class GroupPurchaseSearchService {

    private final ElasticsearchOperations operations;
    private final GroupPurchaseSearchQueryFactory groupPurchaseSearchQueryFactory;
    private final ElasticsearchExceptionTranslator exceptionTranslator;
    private static final long[] SEARCH_RETRY_DELAYS_MS = {200L, 500L};

    public void createGroupPurchaseIndex() {
        try {
            IndexOperations ops = operations.indexOps(GroupPurchaseDocument.class);

            if (!ops.exists()) {
                Document settings = Document.create();
                settings.put("index.number_of_shards", 1);
                settings.put("index.number_of_replicas", 0);
                ops.create(settings);
                ops.putMapping(ops.createMapping(GroupPurchaseDocument.class));
            }
        } catch (Exception e) {
            throw exceptionTranslator.translate(e);
        }
    }

    public void deleteGroupPurchaseIndex() {
        try {
            IndexOperations ops = operations.indexOps(GroupPurchaseDocument.class);
            if (!ops.exists()) {
                return;
            }
            ops.delete();
        } catch (Exception e) {
            throw exceptionTranslator.translate(e);
        }
    }

    @ServiceLog
    public PageResponse<GroupPurchaseDocumentIdInfo> searchGroupPurchaseDocument(
            String keyword,
            String status,
            UUID memberId,
            String category,
            Pageable pageable
    ) {
        String sellerId = memberId != null ? memberId.toString() : null;
        try {
            NativeQuery query = groupPurchaseSearchQueryFactory.createSearchQuery(keyword, status, sellerId, category, pageable);

            SearchHits<GroupPurchaseDocument> hits = searchWithRetry(query);

            SearchPage<GroupPurchaseDocument> searchPage = SearchHitSupport.searchPageFor(hits, pageable);
            Page<GroupPurchaseDocumentIdInfo> mappedPage = searchPage
                    .map(hit -> GroupPurchaseDocumentIdInfo.from(hit.getId()));
            return PageResponse.from(mappedPage);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw exceptionTranslator.translate(e);
        }
    }

    @ServiceLog
    public PageResponse<GroupPurchaseDocumentIdInfo> searchAllGroupPurchaseDocument(
            String keyword,
            String status,
            String category,
            Pageable pageable
    ) {
        try {
            NativeQuery query = groupPurchaseSearchQueryFactory.createSearchQuery(keyword, status, null, category, pageable);

            SearchHits<GroupPurchaseDocument> hits = searchWithRetry(query);

            SearchPage<GroupPurchaseDocument> searchPage = SearchHitSupport.searchPageFor(hits, pageable);
            Page<GroupPurchaseDocumentIdInfo> mappedPage = searchPage
                    .map(hit -> GroupPurchaseDocumentIdInfo.from(hit.getId()));
            return PageResponse.from(mappedPage);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw exceptionTranslator.translate(e);
        }
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
}
