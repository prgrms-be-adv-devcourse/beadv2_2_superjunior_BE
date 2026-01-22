package store._0982.member.application.notification.dto.kafka.settlement;

import store._0982.common.kafka.dto.SettlementDoneEvent;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationType;

import java.math.BigDecimal;
import java.util.UUID;

public record SettlementDeferredCommand(
        UUID settlementId,
        UUID sellerId,
        BigDecimal settlementAmount,
        int month
) implements Notifiable {

    public static SettlementDeferredCommand from(SettlementDoneEvent event) {
        if (event.getStatus() != SettlementDoneEvent.Status.DEFERRED) {
            throw new IllegalStateException();
        }
        return new SettlementDeferredCommand(
                event.getId(),
                event.getSellerId(),
                event.getSettlementAmount(),
                event.getStart().getMonthValue()
        );
    }

    @Override
    public UUID memberId() {
        return sellerId;
    }

    @Override
    public NotificationContent content() {
        return new NotificationContent(
                NotificationType.SETTLEMENT_COMPLETED,
                "월간 정산 이월",
                String.format("%d월의 정산금(%,d원)이 너무 적어 다음 달에 정산될 예정입니다.", month, settlementAmount.longValue()),
                settlementId
        );
    }
}
