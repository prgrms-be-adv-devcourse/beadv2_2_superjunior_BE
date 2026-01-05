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
import store._0982.elasticsearch.application.dto.ProductDocumentInfo;
import store._0982.elasticsearch.domain.ProductDocument;
import store._0982.elasticsearch.exception.ElasticsearchExceptionTranslator;
import store._0982.elasticsearch.infrastructure.queryfactory.ProductSearchQueryFactory;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ProductSearchService {

    private final ElasticsearchOperations operations;
    private final ProductSearchQueryFactory productSearchQueryFactory;
    private final ElasticsearchExceptionTranslator exceptionTranslator;
    private static final long[] SEARCH_RETRY_DELAYS_MS = {200L, 500L};

    public void createProductIndex() {
        try {
            IndexOperations ops = operations.indexOps(ProductDocument.class);

            if (!ops.exists()) {
                Document settings = Document.create();
                settings.put("index.number_of_shards", 1);
                settings.put("index.number_of_replicas", 0);
                ops.create(settings);
                ops.putMapping(ops.createMapping(ProductDocument.class));
            }
        } catch (Exception e) {
            throw exceptionTranslator.translate(e);
        }
    }

    public void deleteProductIndex() {
        try {
            IndexOperations ops = operations.indexOps(ProductDocument.class);
            if (!ops.exists()) {
                return;
            }
            ops.delete();
        } catch (Exception e) {
            throw exceptionTranslator.translate(e);
        }
    }

    @ServiceLog
    public PageResponse<ProductDocumentInfo> searchProductDocument(String keyword,
                                                                   UUID sellerId,
                                                                   String category,
                                                                   Pageable pageable) {

        try {
            NativeQuery query = productSearchQueryFactory.build(keyword, sellerId, category, pageable);

            SearchHits<ProductDocument> hits = searchWithRetry(query);

            SearchPage<ProductDocument> searchPage = SearchHitSupport.searchPageFor(hits, pageable);

            Page<ProductDocumentInfo> mappedPage = searchPage
                    .map(hit -> ProductDocumentInfo.from(hit.getContent()));

            return PageResponse.from(mappedPage);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw exceptionTranslator.translate(e);
        }
    }

    private SearchHits<ProductDocument> searchWithRetry(NativeQuery query) {
        int attempts = SEARCH_RETRY_DELAYS_MS.length + 1;
        for (int i = 0; i < attempts; i++) {
            try {
                return operations.search(query, ProductDocument.class);
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
