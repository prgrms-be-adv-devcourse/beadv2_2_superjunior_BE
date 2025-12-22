package store._0982.product.application.product.dto;

import store._0982.product.domain.product.Product;
import store._0982.product.domain.product.ProductCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductDetailInfo(
        UUID productId,
        String name,
        Long price,
        ProductCategory category,
        String description,
        int stock,
        String originalLink,
        UUID sellerId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ProductDetailInfo from(Product product) {
        return new ProductDetailInfo(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                product.getDescription(),
                product.getStock(),
                product.getOriginalUrl(),
                product.getSellerId(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
