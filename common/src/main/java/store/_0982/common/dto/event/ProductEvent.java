package store._0982.common.dto.event;

import java.util.UUID;

public record ProductEvent(
        UUID productId,
        String name,
        int price,
        String category,
        String description,
        int stock,
        String originalUrl,
        UUID sellerId,
        String createdAt,
        String updatedAt
) {
}
