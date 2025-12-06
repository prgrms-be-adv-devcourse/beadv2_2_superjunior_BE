package store._0982.point.point.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import store._0982.point.point.application.dto.PointChargeConfirmCommand;

import java.util.UUID;

public record PointChargeConfirmRequest(
        @NotNull UUID orderId,
        @Positive int amount,
        @NotBlank String paymentKey
) {
    public PointChargeConfirmCommand toCommand(){
        return new PointChargeConfirmCommand(orderId, amount, paymentKey);
    }
}
