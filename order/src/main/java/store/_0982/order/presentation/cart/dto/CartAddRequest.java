package store._0982.order.presentation.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import store._0982.order.application.cart.dto.CartAddCommand;

import java.util.UUID;

public record CartAddRequest(@NotNull UUID groupPurchaseId, @NotNull @Min(value = 1) Integer quantity

) {
    public CartAddCommand toCommand(UUID memberId) {
        return new CartAddCommand(memberId, groupPurchaseId, quantity);
    }
}

