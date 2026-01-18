package store._0982.point.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import store._0982.point.application.dto.pg.PgCreateCommand;

import java.util.UUID;

public record PgCreateRequest(
        @NotNull UUID orderId,
        @Positive long amount
) {
    public PgCreateCommand toCommand() {
        return new PgCreateCommand(orderId, amount);
    }
}
