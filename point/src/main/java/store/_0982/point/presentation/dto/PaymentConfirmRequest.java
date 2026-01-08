package store._0982.point.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import store._0982.point.application.dto.PaymentConfirmCommand;

import java.util.UUID;

public record PaymentConfirmRequest(
        @NotNull UUID orderId,
        @Positive long amount,
        @NotBlank String paymentKey
) {
    public PaymentConfirmCommand toCommand(){
        return new PaymentConfirmCommand(orderId, amount, paymentKey);
    }
}
