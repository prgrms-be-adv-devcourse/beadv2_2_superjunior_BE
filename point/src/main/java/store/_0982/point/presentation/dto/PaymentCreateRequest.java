package store._0982.point.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import store._0982.point.application.dto.PaymentCreateCommand;

import java.util.UUID;

public record PaymentCreateRequest(
        @NotNull UUID orderId,
        @NotNull UUID pgOrderId,
        @Positive long amount
) {
    public PaymentCreateCommand toCommand(){
        return new PaymentCreateCommand(orderId, pgOrderId, amount);
    }
}
