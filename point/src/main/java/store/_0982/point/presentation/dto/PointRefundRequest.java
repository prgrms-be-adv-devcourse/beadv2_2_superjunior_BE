package store._0982.point.presentation.dto;

import jakarta.validation.constraints.NotNull;
import store._0982.point.application.dto.PointRefundCommand;

import java.util.UUID;

public record PointRefundRequest(
        @NotNull UUID orderId,
        String cancelReason
) {
    public PointRefundCommand toCommand(){
        return new PointRefundCommand(orderId, cancelReason);
    }
}
