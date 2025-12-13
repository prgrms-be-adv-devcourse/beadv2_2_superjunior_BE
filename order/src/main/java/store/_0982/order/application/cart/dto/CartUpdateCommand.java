package store._0982.order.application.cart.dto;

import java.util.UUID;

public record CartUpdateCommand(UUID memberId, UUID cartId, int quantity) {
}
