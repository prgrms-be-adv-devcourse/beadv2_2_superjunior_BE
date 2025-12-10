package store._0982.notification.application;

import store._0982.common.kafka.dto.OrderEvent;
import store._0982.common.kafka.dto.PointEvent;
import store._0982.notification.domain.*;

public final class NotificationCreator {
    public static Notification create(OrderEvent event, NotificationType type, String title, String message, NotificationChannel channel) {
        return Notification.builder()
                .memberId(event.getMemberId())
                .type(type)
                .channel(channel)
                .title(title)
                .message(message)
                .referenceType(ReferenceType.ORDER)
                .status(NotificationStatus.SENT)
                .referenceId(event.getId())
                .build();
    }

    public static Notification create(PointEvent event, NotificationType type, String title, String message, NotificationChannel channel) {
        return Notification.builder()
                .memberId(event.getMemberId())
                .type(type)
                .channel(channel)
                .title(title)
                .message(message)
                .referenceType(ReferenceType.POINT)
                .status(NotificationStatus.SENT)
                .referenceId(event.getId())
                .build();
    }

    private NotificationCreator() {
    }
}
