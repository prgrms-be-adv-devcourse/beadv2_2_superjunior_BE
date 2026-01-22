package store._0982.point.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import store._0982.point.client.dto.TossPaymentInfo;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record TossWebhookRequest(
        @NotBlank String eventType,
        @NotNull LocalDateTime createdAt,
        @NotNull TossPaymentInfo data
) {
    public OffsetDateTime createdAtWithOffset() {
        return OffsetDateTime.of(createdAt, ZoneOffset.of("+9"));
    }
}
