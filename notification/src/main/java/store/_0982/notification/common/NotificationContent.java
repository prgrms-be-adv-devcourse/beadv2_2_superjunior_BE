package store._0982.notification.common;

import store._0982.notification.domain.NotificationType;

public record NotificationContent(
        NotificationType type,
        String title,
        String message
) {
}
