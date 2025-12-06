package store._0982.point.point.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import store._0982.point.point.application.dto.PointChargeFailCommand;

import java.util.UUID;

public record PointChargeFailRequest(
        @NotNull UUID orderId,
        @NotBlank String paymentKey,
        String errorCode,
        String errorMessage,
        int amount,
        String rawPayload
) {
    public PointChargeFailCommand toCommand() {
        return new PointChargeFailCommand(orderId, paymentKey, errorCode, errorMessage, amount, rawPayload);
    }
}
