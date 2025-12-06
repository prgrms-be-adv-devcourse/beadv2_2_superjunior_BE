package store._0982.point.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import store._0982.point.application.dto.PointChargeFailCommand;

import java.util.UUID;

public record PointChargeFailRequest(
        @NotNull UUID orderId,
        @NotBlank String paymentKey,
        String errorCode,
        String errorMessage,
        @Positive int amount,
        @NotBlank String rawPayload
) {
    public PointChargeFailCommand toCommand() {
        return new PointChargeFailCommand(orderId, paymentKey, errorCode, errorMessage, amount, rawPayload);
    }
}
