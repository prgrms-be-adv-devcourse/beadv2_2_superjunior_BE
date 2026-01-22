package store._0982.member.application.notification.dto;

import store._0982.common.kafka.dto.OrderConfirmedEvent;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationType;

import java.util.UUID;

public record OrderConfirmedCommand(
        UUID memberId,
        UUID orderId,
        String productName
) implements Notifiable {

    public static OrderConfirmedCommand from(OrderConfirmedEvent event) {
        return new OrderConfirmedCommand(event.getMemberId(), event.getOrderId(), event.getProductName());
    }

    @Override
    public NotificationContent content() {
        return new NotificationContent(
                NotificationType.ORDER_CONFIRMED,
                "상품 구매 확정 완료",
                String.format("상품 %s에 대한 구매 확정이 완료되었습니다.", productName),
                orderId
        );
    }
}
