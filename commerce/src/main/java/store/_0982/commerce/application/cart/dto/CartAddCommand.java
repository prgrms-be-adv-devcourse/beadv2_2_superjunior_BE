package store._0982.commerce.application.cart.dto;

import java.util.UUID;

public record CartAddCommand(
        UUID memberId,
        UUID groupPurchaseId,
        Integer quantity
) {
}

