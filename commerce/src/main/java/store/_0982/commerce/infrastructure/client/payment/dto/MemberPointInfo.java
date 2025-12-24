package store._0982.commerce.infrastructure.client.payment.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MemberPointInfo(
        UUID memberId,
        int pointBalance,
        OffsetDateTime lastUsedAt
) {
}
