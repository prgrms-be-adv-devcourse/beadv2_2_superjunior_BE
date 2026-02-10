package store._0982.commerce.application.product.dto;

import store._0982.common.domain.product.Product;

import java.util.UUID;

public record ProductInfoForVector(
        UUID productId,
        String name,
        String description,
        String category
) {
    public static ProductInfoForVector from(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductInfoForVector(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getCategory() == null ? null : product.getCategory().name()
        );
    }
}
