package store._0982.commerce.presentation.product.dto;

import jakarta.validation.constraints.*;
import store._0982.commerce.application.product.dto.ProductRegisterCommand;
import store._0982.commerce.domain.product.ProductCategory;

import java.util.UUID;

public record ProductRegisterRequest(
        @NotBlank
        String name,

        @Positive
        Long price,

        @NotNull
        ProductCategory category,

        @NotBlank
        String description,

        @Positive
        int stock,

        String originalUrl,

        @NotBlank
        String idempotencyKey
) {

    public ProductRegisterCommand toCommand(UUID sellerId) {
        return new ProductRegisterCommand(name, price, category, description, stock, originalUrl, idempotencyKey, sellerId);
    }
}
