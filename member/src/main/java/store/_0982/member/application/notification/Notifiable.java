package store._0982.member.application.notification;

import store._0982.member.common.notification.NotificationContent;

import java.util.UUID;

public interface Notifiable {

    NotificationContent content();

    UUID memberId();
}
