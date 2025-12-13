package store._0982.order.client.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProfileInfo(
        UUID memberId,
        String email,
        String name,
        OffsetDateTime createdAt,
        String role,
        String imageUrl,
        String phoneNumber
) {
}
