package store._0982.commerce.application.product.dto;

import store._0982.commerce.domain.product.ProductCategory;

public record ProductUpdateCommand(
        String name,
        Long price,
        ProductCategory category,
        String description,
        int stock,
        String originalLink
) {
}
