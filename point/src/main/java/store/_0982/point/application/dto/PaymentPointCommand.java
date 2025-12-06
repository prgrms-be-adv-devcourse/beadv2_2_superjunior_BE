package store._0982.point.application.dto;

import java.util.UUID;

public record PaymentPointCommand(
        UUID orderId,
        int amount
) {
}
