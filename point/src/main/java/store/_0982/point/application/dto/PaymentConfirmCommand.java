package store._0982.point.application.dto;

import java.util.UUID;

public record PaymentConfirmCommand(
        UUID orderId,
        long amount,
        String paymentKey
) {
}
