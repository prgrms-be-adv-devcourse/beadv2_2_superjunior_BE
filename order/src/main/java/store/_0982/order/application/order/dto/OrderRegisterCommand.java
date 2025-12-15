package store._0982.order.application.order.dto;

import java.util.UUID;

public record OrderRegisterCommand(
        int quantity,
        String address,
        String addressDetail,
        String postalCode,
        String receiverName,
        UUID sellerId,
        UUID groupPurchaseId
) {
}
