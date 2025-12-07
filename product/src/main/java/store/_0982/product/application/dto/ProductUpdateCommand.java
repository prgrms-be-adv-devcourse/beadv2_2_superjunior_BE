package store._0982.product.application.dto;

import store._0982.product.domain.ProductCategory;

public record ProductUpdateCommand(
        String name,
        int price,
        ProductCategory category,
        String description,
        int stock,
        String originalLink
) {
}
