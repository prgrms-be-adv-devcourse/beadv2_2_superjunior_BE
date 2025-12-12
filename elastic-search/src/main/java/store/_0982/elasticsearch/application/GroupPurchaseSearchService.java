package store._0982.elasticsearch.application;

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
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
import store._0982.elasticsearch.infrastructure.GroupPurchaseRepository;


@RequiredArgsConstructor
@Service
public class GroupPurchaseSearchService {

    private final ElasticsearchOperations operations;
    private final GroupPurchaseRepository groupPurchaseRepository;

    public void createGroupPurchaseIndex() {
        IndexOperations ops = operations.indexOps(GroupPurchaseDocument.class);

        if (ops.exists()) {
            throw new CustomException(CustomErrorCode.ALREADY_EXIST_INDEX);
        }
        Document settings = Document.create();
        settings.put("index.number_of_shards", 1);
        settings.put("index.number_of_replicas", 0);
        ops.create(settings);
        ops.putMapping(ops.createMapping(GroupPurchaseDocument.class));
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
            Pageable pageable
    ) {
        NativeQuery query;
        // keyword 입력이 없으면 전체 문서 검색
        if (keyword == null || keyword.isBlank()) {
            query = NativeQuery.builder()
                    .withQuery(q -> q
                            .bool(b -> b
                                    .must(m -> m.matchAll(mm -> mm))
                                    .filter(f -> f
                                            .term(t -> t
                                                    .field("status")
                                                    .value(status)
                                            )
                                    )
                            )
                    )
                    .withPageable(pageable)
                    .build();
        }
        // keyword를 입력하면 title, description 기준 keyword OR 매칭(multi_match) 검색
        else {
            query = NativeQuery.builder()
                    .withQuery(q ->
                            q.bool(b -> b
                                    .must(m -> m
                                            .multiMatch(mm -> mm
                                                    .query(keyword)
                                                    .fields("title", "description")
                                                    .type(TextQueryType.BestFields)
                                            )
                                    )
                                    .filter(f -> f
                                            .term(t -> t
                                                    .field("status")
                                                    .value(status)
                                            )
                                    )
                            ))
                    .withPageable(pageable)
                    .build();
        }

        SearchHits<GroupPurchaseDocument> hits = operations.search(query, GroupPurchaseDocument.class);

        SearchPage<GroupPurchaseDocument> searchPage = SearchHitSupport.searchPageFor(hits, pageable);

        Page<GroupPurchaseDocumentInfo> mappedPage = searchPage
                .map(hit -> GroupPurchaseDocumentInfo.from(hit.getContent()));

        return PageResponse.from(mappedPage);
    }
}
