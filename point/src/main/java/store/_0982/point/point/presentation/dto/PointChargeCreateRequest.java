package store._0982.point.point.presentation.dto;

import store._0982.point.point.application.dto.PaymentPointCommand;

import java.util.UUID;

public record PointChargeCreateRequest(
        UUID orderId,
        String paymentKey,
        int amount,
        String paymentMethod
) {
    public PaymentPointCommand toCommand(){
        return new PaymentPointCommand(orderId, paymentKey, amount, paymentMethod);
    }
}
