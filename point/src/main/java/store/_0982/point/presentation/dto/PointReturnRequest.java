package store._0982.point.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import store._0982.point.application.dto.PointReturnCommand;

import java.util.UUID;

public record PointReturnRequest(
        @NotNull UUID idempotencyKey,
        @NotNull UUID orderId,
        @Positive long amount
) {
    public PointReturnCommand toCommand() {
        return new PointReturnCommand(idempotencyKey, orderId, amount);
    }
}
