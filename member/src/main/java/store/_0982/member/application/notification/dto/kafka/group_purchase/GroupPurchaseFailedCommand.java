package store._0982.member.application.notification.dto.kafka.group_purchase;

import store._0982.member.application.notification.BulkNotifiable;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record GroupPurchaseFailedCommand(
        UUID groupPurchaseId,
        UUID sellerId,
        String title,
        List<UUID> participantIds
) implements BulkNotifiable {

    public static GroupPurchaseFailedCommand of(
            UUID groupPurchaseId,
            UUID sellerId,
            String title,
            List<UUID> participantIds
    ) {
        return new GroupPurchaseFailedCommand(groupPurchaseId, sellerId, title, participantIds);
    }

    @Override
    public List<Notifiable> notifiables() {
        List<Notifiable> notifications = new ArrayList<>();

        notifications.add(Notifiable.of(
                sellerId,
                new NotificationContent(
                        NotificationType.GROUP_PURCHASE_FAILED,
                        "공동 구매 실패",
                        String.format("'%s' 공동 구매가 목표 수량 미달로 취소되었습니다.", title),
                        groupPurchaseId
                )
        ));

        NotificationContent buyerContent = new NotificationContent(
                NotificationType.GROUP_PURCHASE_FAILED,
                "공동 구매 실패",
                String.format("참여하신 '%s' 공동 구매가 취소되어 환불 처리됩니다.", title),
                groupPurchaseId
        );
        participantIds.forEach(buyerId -> notifications.add(Notifiable.of(buyerId, buyerContent)));

        return notifications;
    }
}
