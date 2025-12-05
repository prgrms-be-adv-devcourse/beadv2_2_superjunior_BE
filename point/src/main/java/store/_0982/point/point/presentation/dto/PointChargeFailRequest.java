package store._0982.point.point.presentation.dto;

import store._0982.point.point.application.dto.PointChargeFailCommand;

import java.util.UUID;

public record PointChargeFailRequest(
        UUID orderId,
        String paymentKey,
        String errorCode,
        String errorMessage,
        int amount,
        String rawPayload
) {
    public PointChargeFailCommand toCommand() {
        return new PointChargeFailCommand(orderId, paymentKey, errorCode, errorMessage, amount, rawPayload);
    }
}