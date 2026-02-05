package store._0982.member.application.notification.dto.kafka.payment;

import store._0982.common.kafka.dto.PaymentChangedEvent;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationType;

import java.util.UUID;

public record PaymentRefundedCommand(
        UUID memberId,
        UUID paymentId,
        long amount
) implements Notifiable {

    public static PaymentRefundedCommand from(PaymentChangedEvent event) {
        if (event.getStatus() != PaymentChangedEvent.Status.REFUNDED) {
            throw new IllegalStateException();
        }
        return new PaymentRefundedCommand(event.getMemberId(), event.getPaymentId(), event.getAmount());
    }

    @Override
    public NotificationContent content() {
        return new NotificationContent(
                NotificationType.PG_REFUNDED,
                "PG 결제 환불",
                String.format("%,d원이 정상적으로 환불되었습니다.", amount),
                paymentId
        );
    }
}
