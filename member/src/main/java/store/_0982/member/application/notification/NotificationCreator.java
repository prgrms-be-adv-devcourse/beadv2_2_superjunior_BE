package store._0982.member.application.notification;

import store._0982.common.kafka.dto.*;
import store._0982.notification.common.notification.NotificationContent;
import store._0982.notification.domain.notification.Notification;
import store._0982.notification.domain.notification.NotificationChannel;
import store._0982.notification.domain.notification.NotificationStatus;
import store._0982.notification.domain.notification.ReferenceType;

public final class NotificationCreator {
    public static Notification create(OrderEvent event, NotificationContent content, NotificationChannel channel) {
        return Notification.builder()
                .memberId(event.getMemberId())
                .type(content.type())
                .channel(channel)
                .title(content.title())
                .message(content.message())
                .referenceType(ReferenceType.ORDER)
                .status(NotificationStatus.SENT)
                .referenceId(event.getId())
                .build();
    }

    public static Notification create(PointEvent event, NotificationContent content, NotificationChannel channel) {
        return Notification.builder()
                .memberId(event.getMemberId())
                .type(content.type())
                .channel(channel)
                .title(content.title())
                .message(content.message())
                .referenceType(ReferenceType.POINT)
                .status(NotificationStatus.SENT)
                .referenceId(event.getId())
                .build();
    }

    public static Notification create(SettlementEvent event, NotificationContent content, NotificationChannel channel) {
        return Notification.builder()
                .memberId(event.getSellerId())
                .type(content.type())
                .channel(channel)
                .title(content.title())
                .message(content.message())
                .referenceType(ReferenceType.SETTLEMENT)
                .status(NotificationStatus.SENT)
                .referenceId(event.getId())
                .build();
    }

    public static Notification create(GroupPurchaseChangedEvent event, NotificationContent content, NotificationChannel channel) {
        return Notification.builder()
                .memberId(event.getSellerId())
                .type(content.type())
                .channel(channel)
                .title(content.title())
                .message(content.message())
                .referenceType(ReferenceType.GROUP_PURCHASE)
                .status(NotificationStatus.SENT)
                .referenceId(event.getId())
                .build();
    }

    private NotificationCreator() {
    }
}
