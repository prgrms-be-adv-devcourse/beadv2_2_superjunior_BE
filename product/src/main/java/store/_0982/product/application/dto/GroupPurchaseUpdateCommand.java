package store._0982.product.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record GroupPurchaseUpdateCommand(
        int minQuantity,
        int maxQuantity,
        String title,
        String description,
        int discountedPrice,
        LocalDateTime startDate,
        LocalDate endDate,
        UUID productId
) {
}
