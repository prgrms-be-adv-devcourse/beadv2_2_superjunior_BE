package store._0982.commerce.application.order.dto;

import java.util.UUID;

public record OrderCancelCommand(
        UUID memberId,
        UUID orderId,
        String reason
) {
}
