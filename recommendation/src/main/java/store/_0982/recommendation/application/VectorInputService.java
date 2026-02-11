package store._0982.recommendation.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.dto.ProductUpsertedEvent;
import store._0982.common.dto.PageResponse;
import store._0982.recommendation.infrastructure.feign.commerce.CommerceProductQueryAdapter;
import store._0982.recommendation.infrastructure.feign.commerce.dto.ProductPageResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorInputService {

    private final EmbeddingService embeddingService;
    private final CommerceProductQueryAdapter commerceProductQueryAdapter;
    private static final int EMBEDDING_BATCH_SIZE = 256;

    public void embedding(int size) {
        int page = 0;
        int iterations = 0;
        long totalElements = -1;
        long processed = 0;
        while (true) {
            PageResponse<ProductPageResponse> response = commerceProductQueryAdapter.fetchPage(page, size);
            if (response == null || response.content() == null || response.content().isEmpty()) {
                break;
            }
            if (totalElements < 0 && response.totalElements() >= 0) {
                totalElements = response.totalElements();
            }

            java.util.ArrayList<ProductUpsertedEvent> batch = new java.util.ArrayList<>(EMBEDDING_BATCH_SIZE);
            for (ProductPageResponse row : response.content()) {
                if (row == null || row.productId() == null) {
                    continue;
                }
                ProductUpsertedEvent.Category category = null;
                if (row.category() != null && !row.category().isBlank()) {
                    try {
                        category = ProductUpsertedEvent.Category.valueOf(row.category());
                    } catch (IllegalArgumentException ex) {
                        log.warn("Unknown category: productId={}, category={}", row.productId(), row.category());
                    }
                }
                ProductUpsertedEvent event = new ProductUpsertedEvent(
                        row.productId(),
                        row.name(),
                        row.description(),
                        category
                );
                batch.add(event);
                if (batch.size() >= EMBEDDING_BATCH_SIZE) {
                    int saved = embeddingService.vectorizeBatch(batch);
                    processed += saved;
                    log.info("Embedding batch saved: total={}, requested={}, saved={}, processed={}, page={}/{}",
                            totalElements < 0 ? "unknown" : totalElements,
                            batch.size(),
                            saved,
                            processed,
                            page + 1,
                            response.totalPages());
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                int saved = embeddingService.vectorizeBatch(batch);
                processed += saved;
                log.info("Embedding batch saved: total={}, requested={}, saved={}, processed={}, page={}/{}",
                        totalElements < 0 ? "unknown" : totalElements,
                        batch.size(),
                        saved,
                        processed,
                        page + 1,
                        response.totalPages());
            }

            iterations++;
            if (response.totalPages() <= 0 || page + 1 >= response.totalPages() || response.last()) {
                break;
            }
            page++;
            if (iterations > 100000) {
                log.warn("Iteration limit reached. stop embedding loop.");
                break;
            }
        }
    }
}
