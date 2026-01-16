package store._0982.ai.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.dto.ProductEmbeddingCompleteEvent;
import store._0982.common.kafka.dto.ProductEmbeddingEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {
    private final EmbeddingModel embeddingModel;

    public ProductEmbeddingCompleteEvent vectorize(ProductEmbeddingEvent event) {
        String input = buildInput(event);
        log.info("{} \n 벡터화", input);
        float[] embedding = embeddingModel.embed(input);
        return new ProductEmbeddingCompleteEvent(event.getProductId(), embedding);
    }

    private String buildInput(ProductEmbeddingEvent event) {
        StringBuilder builder = new StringBuilder();
        if (event.getName() != null && !event.getName().isBlank()) {
            builder.append("상품명: ").append(event.getName()).append('\n');
        }
        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            builder.append("상품설명: ").append(event.getDescription()).append('\n');
        }
        if (event.getCategory() != null && !event.getCategory().isBlank()) {
            builder.append("카테고리: ").append(event.getCategory());
        }
        return builder.toString();
    }
}
