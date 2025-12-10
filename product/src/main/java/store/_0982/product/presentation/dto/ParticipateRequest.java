package store._0982.product.presentation.dto;

import jakarta.validation.constraints.Positive;

public record ParticipateRequest(
        @Positive
        int quantity
) {
}
