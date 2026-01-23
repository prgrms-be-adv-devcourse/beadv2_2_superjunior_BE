package store._0982.member.application.notification.dto;


import store._0982.member.domain.notification.Notification;
import store._0982.member.domain.notification.constant.NotificationStatus;
import store._0982.member.domain.notification.constant.NotificationType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for {@link Notification}
 */
public record NotificationInfo(
        UUID id,
        UUID memberId,
        NotificationType type,
        String title,
        String message,
        String failureMessage,
        NotificationStatus status,
        UUID referenceId,
        OffsetDateTime createdAt) {
    public static NotificationInfo from(Notification notification) {
        return new NotificationInfo(
                notification.getId(),
                notification.getMemberId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getFailureMessage(),
                notification.getStatus(),
                notification.getReferenceId(),
                notification.getCreatedAt());
    }
}
