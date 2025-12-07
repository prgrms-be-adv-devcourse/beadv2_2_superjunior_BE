package store._0982.product.application.dto;

import store._0982.product.domain.ProductCategory;

import java.util.UUID;

public record ProductRegisterCommand(
        String name,
        int price,
        ProductCategory category,
        String description,
        int stock,
        String originalUrl,
        UUID sellerId,
        String memberRole
) {
}
