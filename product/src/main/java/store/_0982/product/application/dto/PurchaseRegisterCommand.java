package store._0982.product.application.dto;

import store._0982.product.domain.GroupPurchaseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseRegisterCommand(
        int minQuantity,
        int maxQuantity,
        String title,
        String description,
        int discountedPrice,
        GroupPurchaseStatus stats,
        LocalDateTime startDate,
        LocalDate endDate,
        UUID productId
) {
}
