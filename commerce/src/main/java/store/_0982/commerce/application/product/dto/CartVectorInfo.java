package store._0982.commerce.application.product.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CartVectorInfo(
        UUID cartId,
        UUID memberId,
        UUID productId,
        String description,
        int quantity,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        float[] productVector
) {

}
