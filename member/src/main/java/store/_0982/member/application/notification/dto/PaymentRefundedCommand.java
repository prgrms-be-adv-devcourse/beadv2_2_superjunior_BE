package store._0982.member.application.notification.dto;

import store._0982.common.kafka.dto.PaymentChangedEvent;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationType;

import java.util.UUID;

public record PaymentRefundedCommand(
        UUID memberId,
        UUID orderId
) implements Notifiable {

    public static PaymentRefundedCommand from(PaymentChangedEvent event) {
        if (event.getStatus() != PaymentChangedEvent.Status.REFUNDED) {
            throw new IllegalStateException();
        }
        return new PaymentRefundedCommand(event.getMemberId(), event.getOrderId());
    }

    // TODO: 정보가 부족해서 더 받아 와야 한다
    @Override
    public NotificationContent content() {
        return new NotificationContent(
                NotificationType.PG_REFUNDED,
                "PG 결제 환불",
                "PG 결제가 환불되었습니다.",
                orderId     // PaymentId로 변경 필요
        );
    }
}
