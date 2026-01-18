package store._0982.commerce.application.order.dto;

import java.util.UUID;

public record OrderCancelCommand(
        UUID orderId,
        UUID memberId,
        String reason
) {
}
