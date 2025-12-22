package store._0982.product.application.product.dto;

import store._0982.product.domain.product.ProductCategory;

public record ProductUpdateCommand(
        String name,
        Long price,
        ProductCategory category,
        String description,
        int stock,
        String originalLink
) {
}
