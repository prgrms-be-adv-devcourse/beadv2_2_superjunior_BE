package store._0982.product.presentation.dto;

import jakarta.validation.constraints.*;
import store._0982.product.application.dto.ProductRegisterCommand;
import store._0982.product.domain.ProductCategory;

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

        String originalUrl
) {

    public ProductRegisterCommand toCommand(UUID sellerId) {
        return new ProductRegisterCommand(name, price, category, description, stock, originalUrl, sellerId);
    }
}
