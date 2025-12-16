package store._0982.order.application.cart.dto;

import java.util.UUID;

public record CartDeleteCommand(UUID cartId, UUID memberId) {
}
