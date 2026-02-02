package store._0982.member.application.notification.dto;

import store._0982.member.application.notification.Notifiable;
import store._0982.member.common.notification.NotificationContent;

import java.util.UUID;

/**
 * {@link Notifiable}의 기본 구현체입니다.
 */
public record SimpleNotification(
        UUID memberId,
        NotificationContent content
) implements Notifiable {

    public static SimpleNotification of(UUID memberId, NotificationContent content) {
        return new SimpleNotification(memberId, content);
    }
}
