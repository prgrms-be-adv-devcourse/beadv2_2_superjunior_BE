package store._0982.product.application.dto;

import store._0982.product.domain.GroupPurchaseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchaseRegisterCommand(
        int minQuantity,
        int maxQuantity,
        String title,
        String description,
        int discountedPrice,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        UUID productId
) {
}
