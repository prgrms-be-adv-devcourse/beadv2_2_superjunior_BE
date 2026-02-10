package store_0982.dummy_data.generate_dummy_obj.commerce.row;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import store._0982.common.domain.product.ProductCategory;

@JsonPropertyOrder({
        "productId",
        "name",
        "price",
        "category",
        "description",
        "stock",
        "originalUrl",
        "sellerId",
        "createdAt",
        "updatedAt",
        "deletedAt",
        "idempotencyKey",
        "imageUrl"
})
public record ProductCsvRow(
        UUID productId,
        String name,
        Long price,
        ProductCategory category,
        String description,
        int stock,
        String originalUrl,
        UUID sellerId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime deletedAt,
        String idempotencyKey,
        String imageUrl
) {
}
