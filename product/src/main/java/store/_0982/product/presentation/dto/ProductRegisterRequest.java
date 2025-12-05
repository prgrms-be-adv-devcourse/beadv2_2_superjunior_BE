package store._0982.product.presentation.dto;

import store._0982.product.application.dto.ProductRegisterCommand;
import store._0982.product.domain.ProductCategory;

import java.util.UUID;

public record ProductRegisterRequest(
        String name,
        int price,
        ProductCategory category,
        String description,
        int stock,
        String originalUrl
) {

    public ProductRegisterCommand toCommand(UUID sellerId, String memberRole) {
        return new ProductRegisterCommand(name, price, category, description, stock, originalUrl, sellerId, memberRole);
    }
}
