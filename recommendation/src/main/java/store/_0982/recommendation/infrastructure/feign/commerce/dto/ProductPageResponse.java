package store._0982.recommendation.infrastructure.feign.commerce.dto;

import java.util.UUID;

public record ProductPageResponse(
        UUID productId,
        String name,
        String description,
        String category
) {
}
