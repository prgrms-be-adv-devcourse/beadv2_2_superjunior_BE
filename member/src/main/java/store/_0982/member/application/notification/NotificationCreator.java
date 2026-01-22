package store._0982.member.application.notification;

import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.common.kafka.dto.OrderChangedEvent;
import store._0982.common.kafka.dto.PointChangedEvent;
import store._0982.common.kafka.dto.SettlementDoneEvent;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.Notification;
import store._0982.member.domain.notification.constant.NotificationChannel;
import store._0982.member.domain.notification.constant.NotificationStatus;

public final class NotificationCreator {
    public static Notification create(OrderChangedEvent event, NotificationContent content, NotificationChannel channel) {
        return Notification.builder()
                .memberId(event.getMemberId())
                .type(content.type())
                .channel(channel)
                .title(content.title())
                .message(content.message())
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
                .status(NotificationStatus.SENT)
                .referenceId(event.getOrderId())
                .build();
    }

    public static Notification create(SettlementDoneEvent event, NotificationContent content, NotificationChannel channel) {
        return Notification.builder()
                .memberId(event.getSellerId())
                .type(content.type())
                .channel(channel)
                .title(content.title())
                .message(content.message())
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
                .status(NotificationStatus.SENT)
                .referenceId(event.getId())
                .build();
    }

    private NotificationCreator() {
    }
}
