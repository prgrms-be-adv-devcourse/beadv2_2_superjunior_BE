package store._0982.member.application.notification.dto.kafka.group_purchase;

import store._0982.member.application.notification.BulkNotifiable;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record GroupPurchaseSuccessCommand(
        UUID groupPurchaseId,
        UUID sellerId,
        String title,
        List<UUID> participantIds
) implements BulkNotifiable {

    public static GroupPurchaseSuccessCommand of(
            UUID groupPurchaseId,
            UUID sellerId,
            String title,
            List<UUID> participantIds
    ) {
        return new GroupPurchaseSuccessCommand(groupPurchaseId, sellerId, title, participantIds);
    }

    @Override
    public List<Notifiable> notifiables() {
        List<Notifiable> notifiables = new ArrayList<>();

        notifiables.add(Notifiable.of(
                sellerId,
                new NotificationContent(
                        NotificationType.GROUP_PURCHASE_SUCCESS,
                        "공동 구매 성공",
                        String.format("'%s' 공동 구매가 성공적으로 마감되었습니다.", title),
                        groupPurchaseId
                )
        ));

        NotificationContent buyerContent = new NotificationContent(
                NotificationType.GROUP_PURCHASE_SUCCESS,
                "공동 구매 성공",
                String.format("참여하신 '%s' 공동 구매가 성공했습니다!", title),
                groupPurchaseId
        );
        participantIds.forEach(buyerId -> notifiables.add(Notifiable.of(buyerId, buyerContent)));

        return notifiables;
    }
}
