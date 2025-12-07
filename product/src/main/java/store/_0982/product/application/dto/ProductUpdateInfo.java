package store._0982.product.application.dto;

import store._0982.product.domain.Product;
import store._0982.product.domain.ProductCategory;

import java.util.UUID;

public record ProductUpdateInfo(
        UUID productId,
        String name,
        int price,
        ProductCategory category,
        String description,
        int stock,
        String originalLink,
        UUID sellerId
) {
    public static ProductUpdateInfo from(Product product){
        return new ProductUpdateInfo(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                product.getDescription(),
                product.getStock(),
                product.getOriginalUrl(),
                product.getSellerId()
        );
    }
}
