package store._0982.point.point.presentation.dto;

import java.util.UUID;

public record PointChargeCreateRequest(
        UUID orderId,
        String paymentKey,
        int amount,
        String paymentMethod
) {
}
