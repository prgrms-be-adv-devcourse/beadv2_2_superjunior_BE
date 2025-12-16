package store._0982.point.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import store._0982.point.application.dto.PointDeductCommand;

import java.util.UUID;

public record PointDeductRequest(
        @NotNull UUID idempotencyKey,
        @NotNull UUID orderId,
        @Positive long amount
) {
    public PointDeductCommand toCommand() {
        return new PointDeductCommand(idempotencyKey, orderId, amount);
    }
}
