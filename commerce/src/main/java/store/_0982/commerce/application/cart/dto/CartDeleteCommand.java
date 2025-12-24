package store._0982.commerce.application.cart.dto;

import java.util.UUID;

public record CartDeleteCommand(UUID cartId, UUID memberId) {
}
