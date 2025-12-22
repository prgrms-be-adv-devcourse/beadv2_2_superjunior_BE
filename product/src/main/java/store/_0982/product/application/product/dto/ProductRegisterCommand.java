package store._0982.product.application.product.dto;

import store._0982.product.domain.product.ProductCategory;

import java.util.UUID;

public record ProductRegisterCommand(
        String name,
        Long price,
        ProductCategory category,
        String description,
        int stock,
        String originalUrl,
        UUID sellerId
) {
}
