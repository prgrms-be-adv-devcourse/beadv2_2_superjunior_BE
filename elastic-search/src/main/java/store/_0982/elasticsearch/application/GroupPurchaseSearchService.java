package store._0982.elasticsearch.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Service;
import store._0982.common.dto.PageResponse;
import store._0982.common.log.ServiceLog;
import store._0982.elasticsearch.application.dto.GroupPurchaseDocumentInfo;
import store._0982.elasticsearch.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;
import store._0982.elasticsearch.domain.GroupPurchaseDocument;
import store._0982.elasticsearch.infrastructure.queryfactory.GroupPurchaseSearchQueryFactory;


@RequiredArgsConstructor
@Service
public class GroupPurchaseSearchService {

    private final ElasticsearchOperations operations;
    private final GroupPurchaseSearchQueryFactory groupPurchaseSearchQueryFactory;

    public void createGroupPurchaseIndex() {
        IndexOperations ops = operations.indexOps(GroupPurchaseDocument.class);

        if (!ops.exists()) {
            Document settings = Document.create();
            settings.put("index.number_of_shards", 1);
            settings.put("index.number_of_replicas", 0);
            ops.create(settings);
            ops.putMapping(ops.createMapping(GroupPurchaseDocument.class));
        }
    }

    public void deleteGroupPurchaseIndex() {
        IndexOperations ops = operations.indexOps(GroupPurchaseDocument.class);
        if (!ops.exists()) {
            throw new CustomException(CustomErrorCode.DONOT_EXIST_INDEX);
        }
        ops.delete();
    }

    @ServiceLog
    public PageResponse<GroupPurchaseDocumentInfo> searchGroupPurchaseDocument(
            String keyword,
            String status,
            String category,
            Pageable pageable
    ) {
        NativeQuery query = groupPurchaseSearchQueryFactory.createSearchQuery(keyword, status, category, pageable);

        SearchHits<GroupPurchaseDocument> hits = operations.search(query, GroupPurchaseDocument.class);

        SearchPage<GroupPurchaseDocument> searchPage = SearchHitSupport.searchPageFor(hits, pageable);

        Page<GroupPurchaseDocumentInfo> mappedPage = searchPage
                .map(hit -> GroupPurchaseDocumentInfo.from(hit.getContent()));

        return PageResponse.from(mappedPage);
    }
}
