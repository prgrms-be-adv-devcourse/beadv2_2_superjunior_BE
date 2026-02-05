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

    private static final int MAX_INPUT_LENGTH = 8000;

    public ProductEmbeddingCompletedEvent vectorize(ProductUpsertedEvent event) {
        String input = buildInput(event)
                .replaceAll("[ \\t]+", " ")   // space, tab만 정리
                .replaceAll("\\n+", "\n")     // 줄바꿈은 하나로 유지
                .trim();

        if (input.length() > MAX_INPUT_LENGTH) {
            log.warn("벡터화 입력 길이 초과, 잘림 처리: productId={}, originalLength={}",
                    event.getProductId(), input.length());
            String truncated = input.substring(0, MAX_INPUT_LENGTH);
            int cut = Math.max(
                    truncated.lastIndexOf(' '),
                    Math.max(truncated.lastIndexOf('\n'), truncated.lastIndexOf('\t'))
            );
            if (cut > 0) {
                truncated = truncated.substring(0, cut);
            }
            input = truncated
                    .replaceAll("[^\\p{IsAlphabetic}\\p{IsHangul}\\d\\)\\]%~+/-]+$", "")
                    .trim();
        }

        log.info("{} \n 벡터화", input);
        float[] embedding = embeddingModel.embed(input);
        return new ProductEmbeddingCompletedEvent(event.getProductId(), embedding);
    }

    private String buildInput(ProductUpsertedEvent event) {
        StringBuilder builder = new StringBuilder();

        if (event.getName() != null && !event.getName().isBlank()) {
            builder.append(event.getName()).append('\n');
        }

        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            builder.append(event.getDescription()).append('\n');
        }

        if (event.getCategory() != null && !event.getCategory().name().isBlank()) {
            builder.append(event.getCategory().name());
        }

        return builder.toString();
    }

}
