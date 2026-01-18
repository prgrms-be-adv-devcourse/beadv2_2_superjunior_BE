package store._0982.point.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import store._0982.point.application.dto.pg.PgFailCommand;

import java.util.UUID;

public record PgFailRequest(
        @NotNull UUID orderId,
        @NotBlank String paymentKey,
        String errorCode,
        String errorMessage,
        @Positive long amount,
        @NotBlank String rawPayload
) {
    public PgFailCommand toCommand() {
        return new PgFailCommand(orderId, paymentKey, errorCode, errorMessage, amount, rawPayload);
    }
}
