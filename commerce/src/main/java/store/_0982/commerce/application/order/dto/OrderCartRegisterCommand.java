package store._0982.commerce.application.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record OrderCartRegisterCommand(
        @NotEmpty List<UUID> cartIds,
        @NotBlank String address,
        @NotBlank String addressDetail,
        @NotBlank String postalCode,
        String receiverName
) {
}

