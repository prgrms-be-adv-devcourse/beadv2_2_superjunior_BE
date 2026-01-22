package store._0982.member.application.notification.dto.kafka.point;

import store._0982.common.kafka.dto.PointChangedEvent;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationType;

import java.util.UUID;

public record PointChargedCommand(
        UUID orderId,
        UUID memberId,
        long amount
) implements Notifiable {

    public static PointChargedCommand from(PointChangedEvent event) {
        if (event.getStatus() != PointChangedEvent.Status.CHARGED) {
            throw new IllegalStateException();
        }
        return new PointChargedCommand(event.getOrderId(), event.getMemberId(), event.getAmount());
    }

    // TODO: 정보가 부족해서 더 받아 와야 한다
    @Override
    public NotificationContent content() {
        return new NotificationContent(
                NotificationType.POINT_CHARGED,
                "포인트 충전 완료",
                String.format("포인트 %,d원이 정상적으로 충전되었습니다.", amount),
                memberId        // TransactionId로 변경 필요
        );
    }
}
