package store._0982.member.common.notification;

import store._0982.notification.domain.notification.NotificationType;

public record NotificationContent(
        NotificationType type,
        String title,
        String message
) {
}
