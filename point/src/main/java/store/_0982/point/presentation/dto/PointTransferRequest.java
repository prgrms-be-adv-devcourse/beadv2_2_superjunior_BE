package store._0982.point.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import store._0982.point.application.dto.point.PointTransferCommand;

import java.util.UUID;

public record PointTransferRequest(
        @Positive long amount,
        @NotNull UUID idempotencyKey
) {
    public PointTransferCommand toCommand() {
        return new PointTransferCommand(amount, idempotencyKey);
    }
}
