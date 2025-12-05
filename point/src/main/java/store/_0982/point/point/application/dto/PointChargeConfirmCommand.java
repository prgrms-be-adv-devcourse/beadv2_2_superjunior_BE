package store._0982.point.point.application.dto;

import java.util.UUID;

public record PointChargeConfirmCommand(
        UUID orderId,
        int amount,
        String paymentKey
) {
}
