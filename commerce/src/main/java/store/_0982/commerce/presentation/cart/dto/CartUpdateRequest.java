package store._0982.commerce.presentation.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import store._0982.commerce.application.cart.dto.CartUpdateCommand;

import java.util.UUID;

public record CartUpdateRequest(@NotNull UUID cartId, @NotNull @Min(value = 1) int quantity) {
    public CartUpdateCommand toCommand(UUID memberId) {
        return new CartUpdateCommand(memberId, cartId, quantity);
    }
}
