package store._0982.commerce.presentation.cart.dto;

import jakarta.validation.constraints.NotNull;
import store._0982.commerce.application.cart.dto.CartDeleteCommand;

import java.util.UUID;

public record CartDeleteRequest(@NotNull UUID cartId) {

    public CartDeleteCommand toCommand(UUID memberId){
        return new CartDeleteCommand(cartId, memberId);
    }
}
