package store._0982.member.application.notification;

import store._0982.member.application.notification.dto.SimpleNotification;
import store._0982.member.common.notification.NotificationContent;

import java.util.UUID;

public interface Notifiable {

    NotificationContent content();

    UUID memberId();

    static Notifiable of(UUID memberId, NotificationContent content) {
        return SimpleNotification.of(memberId, content);
    }
}
