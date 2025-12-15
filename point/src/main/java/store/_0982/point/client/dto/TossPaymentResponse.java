package store._0982.point.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResponse(
        String paymentKey,
        UUID orderId,
        @JsonProperty("totalAmount")
        long amount,
        String method,
        String status,
        OffsetDateTime requestedAt,
        OffsetDateTime approvedAt,
        List<CancelInfo> cancels
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CancelInfo(
            long cancelAmount,
            String cancelReason,
            OffsetDateTime canceledAt
    ) {
    }
}
