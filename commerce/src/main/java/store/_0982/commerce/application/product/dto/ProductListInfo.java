package store._0982.commerce.application.product.dto;

import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductListInfo(
        UUID productId,
        String name,
        Long price,
        String imageUrl,
        ProductCategory category,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ProductListInfo from(Product product){
        return new ProductListInfo(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                product.getCategory(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
