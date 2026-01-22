package store._0982.member.application.notification.dto;

import store._0982.common.kafka.dto.PaymentChangedEvent;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationType;

import java.util.UUID;

public record PaymentFailedCommand(
        UUID memberId,
        UUID orderId
) implements Notifiable {

    public static PaymentFailedCommand from(PaymentChangedEvent event) {
        if (event.getStatus() != PaymentChangedEvent.Status.PAYMENT_FAILED) {
            throw new IllegalStateException();
        }
        return new PaymentFailedCommand(event.getMemberId(), event.getOrderId());
    }

    // TODO: 정보가 부족해서 더 받아 와야 한다
    @Override
    public NotificationContent content() {
        return new NotificationContent(
                NotificationType.PG_FAILED,
                "PG 결제 실패",
                "PG 결제에 실패했습니다.",
                orderId     // PaymentId로 수정 필요
        );
    }
}
