package store._0982.member.application.notification.dto;

import java.util.UUID;

public record OrderCompletedCommand(
        UUID orderId,
        UUID memberId,
        String productName
) {
}
