package store._0982.member.application.notification.dto.kafka.settlement;

import store._0982.common.kafka.dto.SettlementDoneEvent;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationType;

import java.math.BigDecimal;
import java.util.UUID;

public record SettlementCompletedCommand(
        UUID settlementId,
        UUID sellerId,
        BigDecimal settlementAmount,
        int month
) implements Notifiable {

    public static SettlementCompletedCommand from(SettlementDoneEvent event) {
        if (event.getStatus() != SettlementDoneEvent.Status.COMPLETED) {
            throw new IllegalStateException();
        }
        return new SettlementCompletedCommand(
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
                "월간 정산 완료",
                String.format("%d월 정산이 완료되어 수수료를 제외한 %,d원이 입금되었습니다.", month, settlementAmount.longValue()),
                settlementId
        );
    }
}
