package store._0982.batch.infrastructure.client.member.dto;

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
