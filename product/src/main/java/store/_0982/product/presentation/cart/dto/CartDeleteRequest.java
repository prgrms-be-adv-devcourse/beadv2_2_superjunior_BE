package store._0982.product.presentation.cart.dto;

import jakarta.validation.constraints.NotNull;
import store._0982.product.application.cart.dto.CartDeleteCommand;

import java.util.UUID;

public record CartDeleteRequest(@NotNull UUID cartId) {

    public CartDeleteCommand toCommand(UUID memberId){
        return new CartDeleteCommand(cartId, memberId);
    }
}
