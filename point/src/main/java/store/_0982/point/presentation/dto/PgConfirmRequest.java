package store._0982.point.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import store._0982.point.application.dto.pg.PgConfirmCommand;

import java.util.UUID;

public record PgConfirmRequest(
        @NotNull UUID orderId,
        @Positive long amount,
        @NotBlank String paymentKey
) {
    public PgConfirmCommand toCommand(){
        return new PgConfirmCommand(orderId, amount, paymentKey);
    }
}
