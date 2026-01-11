package store._0982.commerce.application.cart.dto;

import store._0982.commerce.domain.cart.Cart;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CartInfo(
        UUID cartId,
        UUID memberId,
        UUID groupPurchaseId,
        int quantity,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static CartInfo from(Cart cart) {
        return new CartInfo(
                cart.getCartId(),
                cart.getMemberId(),
                cart.getGroupPurchaseId(),
                cart.getQuantity(),
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }
}

