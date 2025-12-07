package store._0982.point.point.presentation.dto;

import store._0982.point.point.application.dto.PointRefundCommand;

import java.util.UUID;

public record PointRefundRequest(
        UUID orderId,
        String cancelReason
) {
    public PointRefundCommand toCommand(){
        return new PointRefundCommand(orderId, cancelReason);
    }
}
