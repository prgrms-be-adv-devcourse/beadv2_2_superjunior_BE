package store._0982.product.application.dto;

import store._0982.product.domain.Product;
import store._0982.product.domain.ProductCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductListInfo(
        UUID productId,
        String name,
        Long price,
        ProductCategory category,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ProductListInfo from(Product product){
        return new ProductListInfo(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
