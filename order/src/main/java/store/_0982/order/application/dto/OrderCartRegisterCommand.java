package store._0982.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import store._0982.order.presentation.dto.OrderCartRegisterRequest;

import java.util.List;
import java.util.UUID;

public record OrderCartRegisterCommand(
        @NotEmpty List<UUID> cardIds,
        @NotBlank String address,
        @NotBlank String addressDetail,
        @NotBlank String postalCode,
        String receiverName
) {
}

