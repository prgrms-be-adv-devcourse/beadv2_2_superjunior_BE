package store._0982.commerce.presentation.order.dto;

import jakarta.validation.constraints.NotBlank;
import store._0982.commerce.application.order.dto.OrderCancelCommand;

import java.util.UUID;

public record OrderCancelRequest (
        @NotBlank
        String reason
){
    public OrderCancelCommand toCommand(UUID memberId, UUID orderId) {
        return new OrderCancelCommand(
                memberId,
                orderId,
                reason
        );
    }
}
