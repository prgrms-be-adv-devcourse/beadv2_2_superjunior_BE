package store._0982.product.presentation.product.dto;

import store._0982.product.application.product.dto.ProductUpdateCommand;
import store._0982.product.domain.product.ProductCategory;

public record ProductUpdateRequest(
        String name,
        Long price,
        ProductCategory category,
        String description,
        int stock,
        String originalLink
) {
    public ProductUpdateCommand toCommand(){
        return new ProductUpdateCommand(name, price, category, description, stock, originalLink);
    }
}
