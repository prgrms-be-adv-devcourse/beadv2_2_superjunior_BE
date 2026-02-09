package store._0982.commerce.presentation.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import store._0982.commerce.application.order.dto.OrderCancelCommand;
import store._0982.common.domain.order.CancelReason;

import java.util.UUID;

public record OrderCancelRequest (

        @NotNull
        CancelReason reason,

        @NotBlank
        String detailReason,

        @NotBlank
        String idempotencyKey
){
    public OrderCancelCommand toCommand(UUID memberId, UUID orderId) {
        return new OrderCancelCommand(
                memberId,
                orderId,
                reason,
                detailReason,
                idempotencyKey
        );
    }
}
