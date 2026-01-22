package store._0982.member.common.notification;


import store._0982.member.domain.notification.constant.NotificationType;

public record NotificationContent(
        NotificationType type,
        String title,
        String message
) {
}
