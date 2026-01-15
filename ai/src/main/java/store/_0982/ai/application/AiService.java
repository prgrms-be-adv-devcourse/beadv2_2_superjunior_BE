package store._0982.ai.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.dto.ProductEmbeddingCompleteEvent;
import store._0982.common.kafka.dto.ProductEmbeddingEvent;

@Service
@RequiredArgsConstructor
public class AiService {
    public ProductEmbeddingCompleteEvent vectorize(ProductEmbeddingEvent event) {
        // 벡터화
        return null;
    }
}
