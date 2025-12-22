package store._0982.commerce.presentation.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import store._0982.commerce.application.order.dto.OrderCartRegisterCommand;

import java.util.List;
import java.util.UUID;

public record OrderCartRegisterRequest(
    @NotEmpty List<UUID> cartIds,
    @NotBlank String address,
    @NotBlank String addressDetail,
    @NotBlank String postalCode,
    String receiverName
) {
    public OrderCartRegisterCommand toCommand(){
        return new OrderCartRegisterCommand(cartIds, address, addressDetail, postalCode, receiverName);
    }
}
