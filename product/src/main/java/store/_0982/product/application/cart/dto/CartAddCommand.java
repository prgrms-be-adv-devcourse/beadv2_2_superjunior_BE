package store._0982.product.application.cart.dto;

import java.util.UUID;

public record CartAddCommand(
        UUID memberId,
        UUID groupPurchaseId,
        Integer quantity
) {
}

