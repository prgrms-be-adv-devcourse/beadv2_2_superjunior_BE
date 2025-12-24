package store._0982.commerce.application.cart.dto;

import java.util.UUID;

public record CartUpdateCommand(UUID memberId, UUID cartId, int quantity) {
}
