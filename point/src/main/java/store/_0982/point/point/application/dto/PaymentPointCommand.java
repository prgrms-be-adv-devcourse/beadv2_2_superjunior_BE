package store._0982.point.point.application.dto;

import java.util.UUID;

public record PaymentPointCommand(
        UUID orderId,
        String paymentKey,
        int amount,
        String paymentMethod
) {
}
