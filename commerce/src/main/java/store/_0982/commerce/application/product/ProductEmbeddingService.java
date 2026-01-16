package store._0982.commerce.application.product;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.product.dto.ProductEmbeddingCompleteInfo;
import store._0982.commerce.domain.product.ProductVector;
import store._0982.commerce.infrastructure.product.ProductVectorJpaRepository;
import store._0982.common.kafka.dto.ProductEmbeddingCompleteEvent;
import store._0982.common.log.ServiceLog;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductEmbeddingService {

    private final ProductVectorJpaRepository vectorizeRepository;

    @Value("${spring.ai.openai.embedding.options.model}")
    private String currentModelVersion;

    @ServiceLog
    @Transactional
    public ProductEmbeddingCompleteInfo updateEmbedding(ProductEmbeddingCompleteEvent completeEvent) {
        ProductVector vector = new ProductVector(completeEvent, currentModelVersion);
        ProductVector saved = vectorizeRepository.save(vector);
        return ProductEmbeddingCompleteInfo.from(saved);
    }
}
