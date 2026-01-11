package store._0982.point.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import store._0982.point.application.dto.PointChargeCommand;

import java.util.UUID;

public record PointChargeRequest(
        @Positive long amount,
        @NotNull UUID idempotencyKey
) {
    public PointChargeCommand toCommand() {
        return new PointChargeCommand(amount, idempotencyKey);
    }
}
