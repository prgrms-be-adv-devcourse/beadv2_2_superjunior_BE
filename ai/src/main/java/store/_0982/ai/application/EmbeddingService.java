package store._0982.ai.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.dto.ProductEmbeddingCompletedEvent;
import store._0982.common.kafka.dto.ProductUpsertedEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {
    private final EmbeddingModel embeddingModel;


    public ProductEmbeddingCompletedEvent vectorize(ProductUpsertedEvent event) {
        String input = buildInput(event)
                .replaceAll("\\s+", " ")
                .trim();

        log.info("{} \n 벡터화", input);
        float[] embedding = embeddingModel.embed(input);
        return new ProductEmbeddingCompletedEvent(event.getProductId(), embedding);
    }

    private String buildInput(ProductUpsertedEvent event) {
        StringBuilder builder = new StringBuilder();
        if (event.getName() != null && !event.getName().isBlank()) {
            builder.append("상품명: ").append(event.getName()).append('\n');
        }
        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            builder.append("상품설명: ").append(event.getDescription()).append('\n');
        }
        if (event.getCategory() != null && !event.getCategory().name().isBlank()) {
            builder.append("카테고리: ").append(event.getCategory().name());
        }
        return builder.toString();
    }

}
