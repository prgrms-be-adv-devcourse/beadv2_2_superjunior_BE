package store._0982.point.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import store._0982.point.application.dto.PaymentFailCommand;

import java.util.UUID;

public record PaymentFailRequest(
        @NotNull UUID orderId,
        @NotBlank String paymentKey,
        String errorCode,
        String errorMessage,
        @Positive long amount,
        @NotBlank String rawPayload
) {
    public PaymentFailCommand toCommand() {
        return new PaymentFailCommand(orderId, paymentKey, errorCode, errorMessage, amount, rawPayload);
    }
}
