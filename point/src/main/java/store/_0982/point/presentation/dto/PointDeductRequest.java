package store._0982.point.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import store._0982.point.application.dto.point.PointDeductCommand;

import java.util.UUID;

public record PointDeductRequest(
        @NotNull UUID idempotencyKey,
        @NotNull UUID orderId,
        @Positive long amount,
        boolean autoCharge
) {
    public PointDeductCommand toCommand() {
        return new PointDeductCommand(idempotencyKey, orderId, amount);
    }
}
