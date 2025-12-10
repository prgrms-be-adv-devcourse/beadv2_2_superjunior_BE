package store._0982.notification.application.dto;

import store._0982.notification.domain.Notification;
import store._0982.notification.domain.NotificationStatus;
import store._0982.notification.domain.NotificationType;

import java.util.UUID;

/**
 * DTO for {@link store._0982.notification.domain.Notification}
 */
public record NotificationInfo(
        UUID id,
        UUID memberId,
        NotificationType type,
        String title,
        String message,
        String failureMessage,
        NotificationStatus status,
        UUID referenceId
) {
    public static NotificationInfo from(Notification notification) {
        return new NotificationInfo(
                notification.getId(),
                notification.getMemberId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getFailureMessage(),
                notification.getStatus(),
                notification.getReferenceId());
    }
}
