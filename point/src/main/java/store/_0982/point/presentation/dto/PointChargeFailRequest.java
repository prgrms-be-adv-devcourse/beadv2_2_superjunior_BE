package store._0982.point.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import store._0982.point.application.dto.PointChargeFailCommand;

import java.util.UUID;

public record PointChargeFailRequest(
        @NotNull UUID orderId,
        @NotBlank String paymentKey,
        String errorCode,
        String errorMessage,
        int amount,
        @NotBlank String rawPayload
) {
    public PointChargeFailCommand toCommand() {
        return new PointChargeFailCommand(orderId, paymentKey, errorCode, errorMessage, amount, rawPayload);
    }
}
