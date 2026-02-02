package store._0982.member.application.notification.dto.kafka.payment;

import store._0982.common.kafka.dto.PaymentChangedEvent;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationType;

import java.util.UUID;

public record PaymentFailedCommand(
        UUID memberId,
        UUID paymentId,
        long amount
) implements Notifiable {

    public static PaymentFailedCommand from(PaymentChangedEvent event) {
        if (event.getStatus() != PaymentChangedEvent.Status.PAYMENT_FAILED) {
            throw new IllegalStateException();
        }
        return new PaymentFailedCommand(event.getMemberId(), event.getPaymentId(), event.getAmount());
    }

    @Override
    public NotificationContent content() {
        return new NotificationContent(
                NotificationType.PG_FAILED,
                "결제 실패",
                String.format("%,d원 결제에 실패했습니다.", amount),
                paymentId
        );
    }
}
