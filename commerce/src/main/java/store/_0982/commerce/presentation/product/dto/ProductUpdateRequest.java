package store._0982.commerce.presentation.product.dto;

import store._0982.commerce.application.product.dto.ProductUpdateCommand;
import store._0982.commerce.domain.product.ProductCategory;

public record ProductUpdateRequest(
        String name,
        Long price,
        ProductCategory category,
        String description,
        int stock,
        String originalLink,
        String imageUrl
) {
    public ProductUpdateCommand toCommand(){
        return new ProductUpdateCommand(name, price, category, description, stock, originalLink, imageUrl);
    }
}
