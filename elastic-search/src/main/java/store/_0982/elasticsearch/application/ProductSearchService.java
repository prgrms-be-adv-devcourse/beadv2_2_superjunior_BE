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
import store._0982.elasticsearch.application.dto.ProductDocumentCommand;
import store._0982.elasticsearch.application.dto.ProductDocumentInfo;
import store._0982.elasticsearch.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;
import store._0982.elasticsearch.domain.ProductDocument;
import store._0982.elasticsearch.infrastructure.ProductRepository;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ProductSearchService {

    private final ElasticsearchOperations operations;
    private final ProductRepository productRepository;

    public void createProductIndex() {
        IndexOperations ops = operations.indexOps(ProductDocument.class);

        if (!ops.exists()) {
            Document settings = Document.create();
            settings.put("index.number_of_shards", 1);
            settings.put("index.number_of_replicas", 0);
            ops.create(settings);
            ops.putMapping(ops.createMapping(ProductDocument.class));
        } else {
            throw new CustomException(CustomErrorCode.ALREADY_EXIST_INDEX);
        }
    }

    public void deleteProductIndex() {
        IndexOperations ops = operations.indexOps(ProductDocument.class);
        if (!ops.exists()) {
            throw new CustomException(CustomErrorCode.DONOT_EXIST_INDEX);
        }
        ops.delete();
    }

    @ServiceLog
    public ProductDocumentInfo saveProductDocument(ProductDocumentCommand command) {
        return ProductDocumentInfo.from(productRepository.save(command.toDocument()));
    }

    @ServiceLog
    public PageResponse<ProductDocumentInfo> searchProductDocument(String keyword,
                                                                   UUID sellerId,
                                                                   String category,
                                                                   Pageable pageable) {
        NativeQuery query;
        // keyword 입력이 없으면 전체 문서 검색
        if (keyword == null || keyword.isBlank()) {
            query = NativeQuery.builder()
                    .withQuery(q -> q
                            .bool(b -> b
                                    .must(m -> m.matchAll(mm -> mm))
                                    .filter(f -> f
                                            .term(t -> t
                                                    .field("sellerId")
                                                    .value(sellerId.toString())
                                            )
                                    )
                                    .filter(f -> f
                                            .term(t -> t
                                                    .field("category")
                                                    .value(category)
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
                                                    .fields("name", "description")
                                                    .type(TextQueryType.BestFields)
                                            )
                                    )
                                    .filter(f -> f
                                            .term(t -> t
                                                    .field("category")
                                                    .value(category)
                                            )
                                    )
                                    .filter(f -> f
                                            .term(t -> t
                                                    .field("sellerId")
                                                    .value(sellerId.toString())
                                            )
                                    )
                            ))
                    .withPageable(pageable)
                    .build();
        }

        SearchHits<ProductDocument> hits = operations.search(query, ProductDocument.class);

        SearchPage<ProductDocument> searchPage = SearchHitSupport.searchPageFor(hits, pageable);

        Page<ProductDocumentInfo> mappedPage = searchPage
                .map(hit -> ProductDocumentInfo.from(hit.getContent()));

        return PageResponse.from(mappedPage);
    }

    public void deleteProductDocument(UUID productId) {
        productRepository.deleteById(productId.toString());
    }
}
