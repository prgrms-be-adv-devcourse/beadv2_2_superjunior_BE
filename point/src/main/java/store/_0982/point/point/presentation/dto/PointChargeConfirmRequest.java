package store._0982.point.point.presentation.dto;

import store._0982.point.point.application.dto.PointChargeConfirmCommand;

import java.util.UUID;

public record PointChargeConfirmRequest(
        UUID orderId,
        int amount,
        String paymentKey
) {
    public PointChargeConfirmCommand toCommand(){
        return new PointChargeConfirmCommand(orderId, amount, paymentKey);
    }
}
