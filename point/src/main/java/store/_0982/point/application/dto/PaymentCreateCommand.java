package store._0982.point.application.dto;

import java.util.UUID;

public record PaymentCreateCommand(
        UUID orderId,
        long amount
) {
}
