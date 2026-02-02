package store._0982.member.application.notification.dto.kafka.order;

import store._0982.common.kafka.dto.OrderCanceledEvent;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationType;

import java.util.UUID;

public record OrderCanceledCommand(
        UUID memberId,
        UUID orderId,
        String cancelReason,
        long amount
) implements Notifiable {

    public static OrderCanceledCommand from(OrderCanceledEvent event) {
        return new OrderCanceledCommand(event.getMemberId(), event.getOrderId(), event.getCancelReason(), event.getAmount());
    }

    @Override
    public NotificationContent content() {
        return new NotificationContent(
                NotificationType.ORDER_CANCELED,
                "상품 구매 취소",
                String.format("상품 구매가 취소되어 %,d원이 환불됩니다.", amount),
                orderId
        );
    }
}
