package store._0982.product.presentation.dto;

import store._0982.product.application.dto.ProductRegisterCommand;
import store._0982.product.domain.ProductCategory;

import java.util.UUID;

public record ProductRegisterRequest(
        String name,
        Long price,
        ProductCategory category,
        String description,
        int stock,
        String originalUrl
) {

    public ProductRegisterCommand toCommand(UUID sellerId) {
        return new ProductRegisterCommand(name, price, category, description, stock, originalUrl, sellerId);
    }
}
