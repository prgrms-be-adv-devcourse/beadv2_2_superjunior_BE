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
import store._0982.elasticsearch.application.dto.ProductDocumentInfo;
import store._0982.elasticsearch.domain.ProductDocument;
import store._0982.elasticsearch.infrastructure.queryfactory.ProductSearchQueryFactory;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ProductSearchService {

    private final ElasticsearchOperations operations;
    private final ProductSearchQueryFactory productSearchQueryFactory;

    public void createProductIndex() {
        IndexOperations ops = operations.indexOps(ProductDocument.class);

        if (!ops.exists()) {
            Document settings = Document.create();
            settings.put("index.number_of_shards", 1);
            settings.put("index.number_of_replicas", 0);
            ops.create(settings);
            ops.putMapping(ops.createMapping(ProductDocument.class));
        }
    }

    public void deleteProductIndex() {
        IndexOperations ops = operations.indexOps(ProductDocument.class);
        if (!ops.exists()) {
            return;
        }
        ops.delete();
    }

    @ServiceLog
    public PageResponse<ProductDocumentInfo> searchProductDocument(String keyword,
                                                                   UUID sellerId,
                                                                   String category,
                                                                   Pageable pageable) {

        NativeQuery query = productSearchQueryFactory.build(keyword, sellerId, category, pageable);

        SearchHits<ProductDocument> hits = operations.search(query, ProductDocument.class);

        SearchPage<ProductDocument> searchPage = SearchHitSupport.searchPageFor(hits, pageable);

        Page<ProductDocumentInfo> mappedPage = searchPage
                .map(hit -> ProductDocumentInfo.from(hit.getContent()));

        return PageResponse.from(mappedPage);
    }
}
