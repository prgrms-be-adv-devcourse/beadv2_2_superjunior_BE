package store._0982.member.common.notification;


import store._0982.member.domain.notification.constant.NotificationType;

import java.util.UUID;

public record NotificationContent(
        NotificationType type,
        String title,
        String message,
        UUID referenceId
) {
}
