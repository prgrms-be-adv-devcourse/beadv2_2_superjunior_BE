package store._0982.member.application.notification;

import store._0982.common.kafka.dto.*;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.Notification;
import store._0982.member.domain.notification.NotificationChannel;
import store._0982.member.domain.notification.NotificationStatus;
import store._0982.member.domain.notification.ReferenceType;

public final class NotificationCreator {
    public static Notification create(OrderChangedEvent event, NotificationContent content, NotificationChannel channel) {
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

    public static Notification create(PointChangedEvent event, NotificationContent content, NotificationChannel channel) {
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

    public static Notification create(SettlementDoneEvent event, NotificationContent content, NotificationChannel channel) {
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

    public static Notification create(GroupPurchaseEvent event, NotificationContent content, NotificationChannel channel) {
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
