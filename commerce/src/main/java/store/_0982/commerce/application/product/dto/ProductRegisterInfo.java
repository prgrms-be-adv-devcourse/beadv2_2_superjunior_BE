package store._0982.commerce.application.product.dto;

import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record  ProductRegisterInfo(
        UUID productId,
        String name,
        Long price,
        ProductCategory category,
        String description,
        int stock,
        String originalUrl,
        UUID sellerId,
        OffsetDateTime createdAt
) {

    public static ProductRegisterInfo from(Product product) {
        return new ProductRegisterInfo(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                product.getDescription(),
                product.getStock(),
                product.getOriginalUrl(),
                product.getSellerId(),
                product.getCreatedAt());
    }
}
