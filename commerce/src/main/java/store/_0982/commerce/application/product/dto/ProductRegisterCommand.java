package store._0982.commerce.application.product.dto;

import store._0982.commerce.domain.product.ProductCategory;

import java.util.UUID;

public record ProductRegisterCommand(
        String name,
        Long price,
        ProductCategory category,
        String description,
        int stock,
        String originalUrl,
        String imageUrl,
        String idempotencyKey,
        UUID sellerId
) {
}
