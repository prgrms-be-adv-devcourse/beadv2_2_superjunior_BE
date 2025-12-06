package store._0982.point.point.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import store._0982.point.point.application.dto.PaymentPointCommand;

import java.util.UUID;

public record PointChargeCreateRequest(
        @NotNull UUID orderId,
        @Positive int amount
) {
    public PaymentPointCommand toCommand(){
        return new PaymentPointCommand(orderId, amount);
    }
}
