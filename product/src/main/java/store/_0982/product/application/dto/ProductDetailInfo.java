package store._0982.product.application.dto;

import store._0982.product.domain.Product;
import store._0982.product.domain.ProductCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductDetailInfo(
        UUID productId,
        String name,
        int price,
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
