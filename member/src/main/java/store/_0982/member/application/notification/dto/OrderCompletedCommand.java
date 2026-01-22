package store._0982.member.application.notification.dto;

import store._0982.common.kafka.dto.OrderCreatedEvent;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationType;

import java.util.UUID;

public record OrderCompletedCommand(
        UUID orderId,
        UUID memberId,
        String productName
) implements Notifiable {

    public static OrderCompletedCommand from(OrderCreatedEvent event) {
        return new OrderCompletedCommand(event.getId(), event.getMemberId(), event.getProductName());
    }

    public NotificationContent content() {
        return new NotificationContent(
                NotificationType.ORDER_COMPLETED,
                "상품 구매 완료",
                String.format("상품 %s의 구매가 정상적으로 완료됐습니다.", productName),
                orderId
        );
    }
}
