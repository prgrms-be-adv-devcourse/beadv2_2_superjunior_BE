package store._0982.product.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import store._0982.product.domain.GroupPurchaseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseUpdateCommand(
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
