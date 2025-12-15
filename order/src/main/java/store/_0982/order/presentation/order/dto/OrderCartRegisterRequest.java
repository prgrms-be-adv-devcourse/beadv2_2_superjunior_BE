package store._0982.order.presentation.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import store._0982.order.application.dto.OrderCartRegisterCommand;

import java.util.List;
import java.util.UUID;

public record OrderCartRegisterRequest(
    @NotEmpty List<UUID> cardIds,
    @NotBlank String address,
    @NotBlank String addressDetail,
    @NotBlank String postalCode,
    String receiverName
) {
    public OrderCartRegisterCommand toCommand(){
        return new OrderCartRegisterCommand(cardIds, address, addressDetail, postalCode, receiverName);
    }
}
