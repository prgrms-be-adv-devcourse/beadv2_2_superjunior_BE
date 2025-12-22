package store._0982.product.application.cart.dto;

import java.util.UUID;

public record CartDeleteCommand(UUID cartId, UUID memberId) {
}
