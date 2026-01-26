package store._0982.commerce.application.product.dto;

import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductCategory;

import java.util.UUID;

public record ProductUpdateInfo(
        UUID productId,
        String name,
        Long price,
        ProductCategory category,
        String description,
        int stock,
        String originalLink,
        String imageUrl,
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
                product.getImageUrl(),
                product.getSellerId()
        );
    }
}
