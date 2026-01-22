package store._0982.member.application.notification.dto.kafka;

import store._0982.common.kafka.dto.SettlementDoneEvent;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationType;

import java.util.UUID;

public record SettlementFailedCommand(
        UUID settlementId,
        UUID sellerId,
        int month
) implements Notifiable {

    public static SettlementFailedCommand from(SettlementDoneEvent event) {
        if (event.getStatus() != SettlementDoneEvent.Status.FAILED) {
            throw new IllegalStateException();
        }
        return new SettlementFailedCommand(
                event.getId(),
                event.getSellerId(),
                event.getStart().getMonthValue()
        );
    }

    @Override
    public UUID memberId() {
        return sellerId;
    }

    // TODO: 실패를 어떻게 알려야 할지 잘 모르겠다
    @Override
    public NotificationContent content() {
        return new NotificationContent(
                NotificationType.SETTLEMENT_COMPLETED,
                "월간 정산 실패",
                String.format("%d월 정산이 정상적으로 완료되지 않아 별도 조치될 예정입니다.", month),
                settlementId
        );
    }
}
