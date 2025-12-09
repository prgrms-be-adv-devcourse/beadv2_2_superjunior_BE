package store._0982.point.presentation.dto;

import jakarta.validation.constraints.Positive;

public record PointReturnRequest(
        @Positive int amount
) {
}
