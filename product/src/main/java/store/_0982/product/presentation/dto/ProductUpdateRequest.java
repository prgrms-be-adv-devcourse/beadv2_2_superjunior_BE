package store._0982.product.presentation.dto;

import store._0982.product.application.dto.ProductUpdateCommand;
import store._0982.product.domain.ProductCategory;

public record ProductUpdateRequest(
        String name,
        int price,
        ProductCategory category,
        String description,
        int stock,
        String originalLink
) {
    public ProductUpdateCommand toCommand(){
        return new ProductUpdateCommand(name, price, category, description, stock, originalLink);
    }
}
